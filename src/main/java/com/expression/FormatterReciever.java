package com.expression;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FormatterReciever extends AbstractBehavior<FormatterCont.Message> {

    private final ActorRef<AkkaMainSystem.Message> mainSystem;
    public static Behavior<FormatterCont.Message> create(ActorRef<AkkaMainSystem.Message> main) {
        return Behaviors.setup(context -> new FormatterReciever(context, main));
    }


    private FormatterReciever(ActorContext<FormatterCont.Message> context, ActorRef<AkkaMainSystem.Message> main) {
        super(context);
        this.mainSystem=main;
    }

    @Override
    public Receive<FormatterCont.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(FormatterCont.Calc.class, this::onExampleMessage)
                .build();
    }

    private Behavior<FormatterCont.Message> onExampleMessage(FormatterCont.Calc msg) {
        //getContext().getLog().info("Got: {}",msg.val());
        this.mainSystem.tell(new AkkaMainSystem.FormatterResult(msg.val()));
        return this;
    }
}
