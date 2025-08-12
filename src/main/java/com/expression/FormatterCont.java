package com.expression;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.text.MessageFormat;

public class FormatterCont extends AbstractBehavior<FormatterCont.Message> {
    public interface Message {
    }

    public enum LeftOrRight {
        Left,
        Right
    }

    public record Calc(String val, LeftOrRight pos) implements Message, PrintAndEvaluate.Message {
    }

    private final ActorRef<FormatterCont.Message> cust;
    private final String firstString;
    private final String operation;
    private final LeftOrRight position;

    public static Behavior<Message> create(String first_value, String operation, ActorRef<Message> cust,
            LeftOrRight pos) {
        return Behaviors.setup(context -> new FormatterCont(context, first_value, operation, cust, pos));
    }

    private FormatterCont(ActorContext<FormatterCont.Message> context, String exp1, String operation,
            ActorRef<FormatterCont.Message> cust_, LeftOrRight position) {
        super(context);
        this.firstString = exp1;
        this.cust = cust_;
        this.operation = operation;
        this.position = position;
    }

    @Override
    public Receive<FormatterCont.Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(Calc.class, this::onCalc)
                .build();
    }

    private Behavior<FormatterCont.Message> onCalc(FormatterCont.Calc msg) {
        if (firstString == null)
            return FormatterCont.create(msg.val, this.operation, this.cust, this.position);
        else {
            if (msg.pos == LeftOrRight.Right)// It is intended that the last arrived value is the right one
                this.cust.tell(new Calc(MessageFormat.format("({0}{1}{2})", firstString, this.operation, msg.val),
                        this.position));
            else
                this.cust.tell(new Calc(MessageFormat.format("({0}{1}{2})", msg.val, this.operation, firstString),
                        this.position));

            return this;
        }
    }
}