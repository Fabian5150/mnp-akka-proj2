package com.expression;


import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class Formatter extends AbstractBehavior<Formatter.Message> {
    public record Message(ActorRef<FormatterCont.Message> cust, Expression expr, FormatterCont.LeftOrRight pos) {
    }

    private record SubExpressionInfo(Expression left, Expression right, String operation) {
    }

    public static Behavior<Formatter.Message> create() {
        return Behaviors.setup(Formatter::new);
    }

    private Formatter(ActorContext<Formatter.Message> context) {
        super(context);
    }

    public Receive<Formatter.Message> createReceive() {
        return newReceiveBuilder().onMessage(Formatter.Message.class, this::onMsg).build();
    }

    private SubExpressionInfo GetOperationFromExpression(Expression e) {

        if (e instanceof Expression.Add)
            return new SubExpressionInfo(((Expression.Add) e).left(), ((Expression.Add) e).right(), "+");
        if (e instanceof Expression.Mul)
            return new SubExpressionInfo(((Expression.Mul) e).left(), ((Expression.Mul) e).right(), "*");
        if (e instanceof Expression.Sub)
            return new SubExpressionInfo(((Expression.Sub) e).left(), ((Expression.Sub) e).right(), "-");

        throw new RuntimeException("Here only non primitive Expression! ");
    }

    private Behavior<Formatter.Message> onMsg(Formatter.Message msg) {
        if (msg.expr instanceof Expression.Val) {
            String value = String.valueOf(((Expression.Val) msg.expr).inner());
            msg.cust.tell(new FormatterCont.Calc(value, msg.pos));
            return this;
        }
        var info = GetOperationFromExpression(msg.expr);
        ActorRef<FormatterCont.Message> cont = this.getContext().spawnAnonymous(
                FormatterCont.create(null, info.operation, msg.cust, msg.pos));
        //As the two formatting doesnt depend on each other, the smallest expression would be done first
        // And sent to continuation. Continuation can thus never make sure that what it first gets is actually the
        //left expression.
        this.getContext().getSelf().tell(new Formatter.Message(cont, info.left, FormatterCont.LeftOrRight.Left));
        getContext().spawnAnonymous(Formatter.create()).tell
                (new Formatter.Message(cont, info.right, FormatterCont.LeftOrRight.Right));

        //this.getContext().getSelf().tell(new Formatter.Message(cont,info.right,  FormatterCont.LeftOrRight.Right));

        return this;
    }

}
