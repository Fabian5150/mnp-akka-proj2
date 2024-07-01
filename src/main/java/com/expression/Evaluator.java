package com.expression;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class Evaluator extends AbstractBehavior<Evaluator.Message> {
    public enum position {
        LEFT,
        RIGHT,
        ROOT
    }

    public enum operation {
        ADD,
        SUB,
        MUL
    }

    public interface Message {
    }

    public static record EvaluatorResult(int res, position myPosition) implements Message {
    }

    public static Behavior<Message> create(ActorRef<PrintAndEvaluate.Message> root, ActorRef<Evaluator.Message> cust, Expression expr, position myPosition) {
        return Behaviors.setup(context -> new Evaluator(context, root, cust, expr, myPosition));
    }


    //Wird später nur initialisiert, wenn diese Evaluatorinstanz von PrintAndEvaluate erzeugt wurde
    ActorRef<PrintAndEvaluate.Message> root;
    //Wird später nur initialisiert, wenn diese Evaluatorinstanz von einem anderen Evaluator erzeugt wurde
    ActorRef<Evaluator.Message> cust;
    Expression expr;

    //myPosition == ROOT => Actor, an den zurückgesendet wird, ist PrintAndEvaluate
    position myPosition;
    operation myOperation;

    //Felder für die später erhaltenen ausgewerteten Ausdrücke
    Integer leftEval;
    Integer rightEval;

    private Evaluator(
            ActorContext<Message> context,
            ActorRef<PrintAndEvaluate.Message> root,
            ActorRef<Evaluator.Message> cust,
            Expression expr,
            position myPosition
        ) {
        super(context);
        this.expr = expr;

        if(root != null) this.root = root;
        if(cust != null) this.cust = cust;
        this.myPosition = myPosition;

        if(expr instanceof Expression.Val){
            sendEvaluatorResult(((Expression.Val) expr).inner());
            return;
        }

        createChildActors(expr);
    }

    /*
    * Erzeugt zwei Childaktoren, falls der erhaltene Ausdruck Add, Mul oder Sub ist
    * */
    private void createChildActors(Expression parent){
        if (parent instanceof Expression.Val) throw new RuntimeException("Here only non primitive Expression!");

        Expression[] res = new Expression[2];

        if (parent instanceof Expression.Add) {
            myOperation = operation.ADD;
            res[0] = ((Expression.Add) parent).left();
            res[1] = ((Expression.Add) parent).right();
        } else if (parent instanceof Expression.Mul) {
            myOperation = operation.MUL;
            res[0] = ((Expression.Mul) parent).left();
            res[1] = ((Expression.Mul) parent).right();
        } else {
            myOperation = operation.SUB;
            res[0] = ((Expression.Sub) parent).left();
            res[1] = ((Expression.Sub) parent).right();
        }

        getContext().spawnAnonymous(Evaluator.create(null, getContext().getSelf(), res[0], position.LEFT));
        getContext().spawnAnonymous(Evaluator.create(null, getContext().getSelf(), res[1], position.RIGHT));
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(EvaluatorResult.class, this::onChildResult)
                .build();
    }

    /*
    * Ermittelt, ob Parent PrintAndEvaluate oder ein anderer Evaluator ist und sendet dementsprechend die richtige Nachricht
    * */
    private void sendEvaluatorResult(int res){
        if(myPosition == position.ROOT){
            root.tell(new PrintAndEvaluate.EvaluatorResult(res));
        } else {
            cust.tell(new Evaluator.EvaluatorResult(res, myPosition));
        }
    }

    private Behavior<Message> onChildResult(EvaluatorResult child) {
        if(child.myPosition == position.LEFT){
            leftEval = child.res;
        } else {
            rightEval = child.res;
        }

        // Die tatsächliche Berechnung, wenn beide Ergebnisse erhalten wurden
        if(leftEval != null && rightEval != null){
            //sleep for 1 sec...

            int res = 0;
            switch (myOperation){
                case ADD -> res = leftEval + rightEval;
                case MUL -> res = leftEval * rightEval;
                case SUB -> res = leftEval - rightEval;
            }

            sendEvaluatorResult(res);
        }

        return this;
    }
}
