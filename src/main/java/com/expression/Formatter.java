package com.expression;





import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;




public class Formatter extends AbstractBehavior<Formatter.Message> {
    public record Message(ActorRef<FormatterCont.Message> cust, Expression expr) {}

    private record SubExpressionInfo(Expression left, Expression right, String operation){}
    public static Behavior<Formatter.Message> create()
    {
        return Behaviors.setup(Formatter::new);
    }
    private Formatter(ActorContext<Formatter.Message> context)
    {
        super(context);
    }
    public Receive<Formatter.Message> createReceive() {
        return newReceiveBuilder().onMessage(Formatter.Message.class, this::onMsg).build();
    }
    private SubExpressionInfo GetOperationFromExpression(Expression e)
    {

        if(e instanceof Expression.Add)
            return new SubExpressionInfo(((Expression.Add) e).left(), ((Expression.Add) e).right(), "+");
        if(e instanceof Expression.Mul)
            return new SubExpressionInfo(((Expression.Mul) e).left(), ((Expression.Mul) e).right(),"*");
        if(e instanceof Expression.Sub)
            return new SubExpressionInfo(((Expression.Sub) e).left(), ((Expression.Sub) e).right(), "-");

        throw new RuntimeException("Here only non primitive Expression! ");
    }

    private Behavior<Formatter.Message> onMsg(Formatter.Message msg)
    {
        if(msg.expr instanceof  Expression.Val)
        {
            String  value = String.valueOf( ((Expression.Val)msg.expr).inner() );
            msg.cust.tell(new FormatterCont.Calc(value));
            return this ;
        }
        var info = GetOperationFromExpression(msg.expr);
        ActorRef<FormatterCont.Message> cont= this.getContext().spawnAnonymous(
                FormatterCont.create(null, info.operation, msg.cust));
        this.getContext().getSelf().tell(new Formatter.Message(cont, info.left));
        this.getContext().getSelf().tell(new Formatter.Message(cont, info.right));

        return this;
    }

}
