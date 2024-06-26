package com.expression;





import akka.actor.typed.javadsl.TimerScheduler;
import akka.actor.typed.Behavior;
import akka.actor.typed.ActorRef;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;




public class Formatter extends AbstractBehavior<Formatter.Message> {
    public record Message(ActorRef<FormatterCont.Message> cust, Expression expr) {};

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

    private Behavior<Formatter.Message> onMsg(Formatter.Message msg)
    {
        if(msg.expr instanceof  Expression.Val)
        {
            String  value = String.valueOf( ((Expression.Val)msg.expr).inner() );
            msg.cust.tell(new FormatterCont.Message(value));
        }
        //msg.cust.tell(new FormatterCont.Message ((Expression.Val) msg.expr).inner());
        return this;
    }

}
