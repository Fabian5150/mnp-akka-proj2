package com.expression;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class AkkaMainSystem extends AbstractBehavior<AkkaMainSystem.Message> {

    public interface Message {};
    public static class Create implements Message {}
    public static record FormatterResult(String res) implements Message {}

    public static Behavior<Message> create() {
        return Behaviors.setup(AkkaMainSystem::new);
    }

    private AkkaMainSystem(ActorContext<Message> context) {
        super(context);
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder().onMessage(FormatterResult.class,this::onFormatterResult)
                .onMessage(Message.class, this::onCreate)
                .build();
    }

    private Behavior<Message> onCreate(Message command) {
        //#create-actors
        //ActorRef<ExampleActor.Message> a = this.getContext().spawn(ExampleActor.create("Alice"), "alice");
        //ActorRef<ExampleTimerActor.Message> b = this.getContext().spawn(ExampleTimerActor.create(), "timeractor");
        //#create-actors

        //a.tell(new ExampleActor.ExampleMessage(this.getContext().getSelf(),"Test123"));
        var expr = Expression.generateExpression(5,6);


        var formatterReciever = getContext().spawnAnonymous(FormatterReciever.create(getContext().getSelf()));
        var formatter = getContext().spawnAnonymous(Formatter.create());
        formatter.tell(new Formatter.Message(formatterReciever, expr, FormatterCont.LeftOrRight.Left));
        getContext().getLog().info(" Expected: {}", expr.toString());
        return this;
    }
    private Behavior<Message> onFormatterResult( FormatterResult res)
    {
        getContext().getLog().info("End result of the formatter: {}", res.res);
        return this;
    }
}
