package com.expression;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;


public class Evaluator extends AbstractBehavior<Evaluator.Message> {

    public record Message(ActorRef<PrintAndEvaluate.Message> cust, Expression expr) {
    }

    public static Behavior<Message> create(ActorRef<PrintAndEvaluate.Message> cust, Expression expr) {
        return Behaviors.setup(context -> new Evaluator(context, cust, expr));
    }

    ActorRef<PrintAndEvaluate.Message> cust;
    Expression expr;

    private Evaluator(ActorContext<Message> context, ActorRef<PrintAndEvaluate.Message> cust, Expression expr) {
        super(context);
        this.cust = cust;
        this.expr = expr;

        if (expr instanceof Expression.Val) {
            cust.tell(new PrintAndEvaluate.EvaluatorResult(((Expression.Val) expr).inner()));
        } else {
            //var left = getContext().spawnAnonymous(Evaluator.create(getContext().getSelf(), expr));
            //var right = getContext().spawnAnonymous(Evaluator.create(getContext().getSelf(), expr));
        }
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
