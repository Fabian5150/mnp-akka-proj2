package com.expression;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FormatterCont extends AbstractBehavior<FormatterCont.Message> {
    public interface Message {};
    public record Calc(/*ActorRef<FormatterCont.Message> cust,*/ String val) implements Message, AkkaMainSystem.Message {}



    private final ActorRef<FormatterCont.Message> cust;
    private final String firstString;
    private final String operation;

    public static Behavior<Message> create(String first_value, String operation, ActorRef<Message> cust) {
        return Behaviors.setup(context -> new FormatterCont(context, first_value,operation, cust));
    }
    private FormatterCont(ActorContext<FormatterCont.Message> context, String exp1, String operation, ActorRef<FormatterCont.Message> cust_)
    {
        super(context);
        this.firstString= exp1;
        this.cust= cust_;
        this.operation = operation ;
    }

    @Override
    public Receive<FormatterCont.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Calc.class, this::onCalc)
                .build();
    }

    private Behavior<FormatterCont.Message> onCalc(FormatterCont.Calc msg)
    {
        if(firstString==null)
            return FormatterCont.create(msg.val, this.operation, this.cust);
        else
        {
            this.cust.tell(new Calc(firstString+" "+this.operation+" "+msg.val));

            return this;
        }
    }
}