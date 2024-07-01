package com.expression;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.sql.Array;


public class Evaluator extends AbstractBehavior<Evaluator.Message> {

    public interface Message {
    }

    public static record EvaluatorResult(int res) implements Message {
    }

    public static Behavior<Message> create(ActorRef<PrintAndEvaluate.Message> root, ActorRef<Evaluator.Message> cust, Expression expr) {
        return Behaviors.setup(context -> new Evaluator(context, root, cust, expr));
    }

    //Wird später nur initialisiert, wenn diese Evaluatorinstanz von PrintAndEvaluate erzeugt wurde
    ActorRef<PrintAndEvaluate.Message> root;
    //Wird später nur initialisiert, wenn diese Evaluatorinstanz von einem anderen Evaluator erzeugt wurde
    ActorRef<Evaluator.Message> cust;
    Expression expr;

    //Referenzen auf die beiden erzeugten Childaktoren (falls welche erzeugt werden)
    ActorRef<Message> leftChild;
    ActorRef<Message> rightChild;

    private Evaluator(ActorContext<Message> context, ActorRef<PrintAndEvaluate.Message> root, ActorRef<Evaluator.Message> cust , Expression expr) {
        super(context);
        this.expr = expr;

        if (!(expr instanceof Expression.Val)) {
            var childExpressions = childExpressions(expr);

            leftChild = getContext().spawnAnonymous(Evaluator.create(null, getContext().getSelf(), childExpressions[0]));
            rightChild = getContext().spawnAnonymous(Evaluator.create(null, getContext().getSelf(), childExpressions[1]));
        }

        //Kümmert sich um weitere Anweisungen, falls die Parent Actor 'Node' PrintAndEvaluate ist
        if(root != null) {
            this.root = root;

            if (expr instanceof Expression.Val) {
                root.tell(new PrintAndEvaluate.EvaluatorResult(((Expression.Val) expr).inner()));
            }
        };

        //Kümmert sich um weitere Anweisungen, falls die Parent Actor 'Node' ein anderer Evaluator ist
        if(cust != null) {
            this.cust = cust;

            if (expr instanceof Expression.Val) {
                cust.tell(new Evaluator.EvaluatorResult(((Expression.Val) expr).inner()));
            }
        };
    }

    private Expression[] childExpressions(Expression parent){
        if(expr instanceof Expression.Val) throw new RuntimeException("Here only non primitive Expression! ");

        Expression[] res = new Expression[2];

        if (expr instanceof Expression.Add) {
            res[0] = ((Expression.Add) expr).left();
            res[1] = ((Expression.Add) expr).right();
        } else if(expr instanceof Expression.Mul){
            res[0] = ((Expression.Mul) expr).left();
            res[1] = ((Expression.Mul) expr).right();
        } else if(expr instanceof Expression.Sub){
            res[0] = ((Expression.Sub) expr).left();
            res[1] = ((Expression.Sub) expr).right();
        }

        return res;
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Message.class, this::onExpression)
                .build();
    }

    private Behavior<Message> onExpression(Message msg) {
        getContext().getLog().info("");
        return this;
    }
}
