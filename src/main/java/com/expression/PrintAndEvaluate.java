package com.expression;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class PrintAndEvaluate extends AbstractBehavior<PrintAndEvaluate.Message> {

    public interface Message {
    }

    public static class Create implements Message {
    }

    public static record FormatterResult(String res) implements Message {
    }

    public static record EvaluatorResult(int res) implements Message {
    }

    // Nur zum Testen der Auswertungszeit
    // private long rootEvaluatorTime;

    public static Behavior<Message> create() {
        return Behaviors.setup(PrintAndEvaluate::new);
    }

    private PrintAndEvaluate(ActorContext<Message> context) {
        super(context);
    }

    @Override
    public Receive<Message> createReceive() {
        return newReceiveBuilder()
                .onMessage(FormatterResult.class, this::onFormatterResult)
                .onMessage(EvaluatorResult.class, this::onEvalutorResult)
                .onMessage(Message.class, this::onCreate)
                .build();
    }

    private Behavior<Message> onCreate(Message command) {
        var expr = Expression.generateExpression(6, 9);

        var formatterReciever = getContext().spawnAnonymous(FormatterReciever.create(getContext().getSelf()));
        var formatter = getContext().spawnAnonymous(Formatter.create());
        formatter.tell(new Formatter.Message(formatterReciever, expr, FormatterCont.LeftOrRight.Left));

        // Erstellt den "Ur-Evaluator". Er erhält eine Referenz auf PrintAndEvaluate und
        // die Expression bei seiner Erstellung
        // Er ist der Actor, der am Ende das Ergebnis an PrintAndEvaluate übergibt.
        // rootEvaluatorTime = System.currentTimeMillis(); //Nur zum Testen der
        // Auswertungszeit
        getContext().spawnAnonymous(Evaluator.create(getContext().getSelf(), null, expr, Evaluator.position.ROOT));

        /*
         * getContext().getLog().info("String expected: {}", expr.toString());
         * getContext().getLog().info("Evaluation expected: {}", expr.eval());
         * getContext().getLog().info("Expected Runtime: {} seconds", expr.runtime());
         */
        return this;
    }

    private Behavior<Message> onFormatterResult(FormatterResult res) {
        getContext().getLog().info("End result of the formatter: {}", res.res);
        return this;
    }

    private Behavior<Message> onEvalutorResult(EvaluatorResult res) {
        getContext().getLog().info("End result of the evaluator: {}", res.res);

        /*
         * //Nur zum Testen der Auswertungszeit
         * long now = System.currentTimeMillis();
         * getContext().getLog().info("Actual Evaluation time: {} seconds",(now -
         * rootEvaluatorTime)/1000);
         */

        return this;
    }
}
