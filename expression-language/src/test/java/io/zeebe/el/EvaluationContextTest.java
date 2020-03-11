/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */
package io.zeebe.el;

import static io.zeebe.test.util.MsgPackUtil.asMsgPack;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.agrona.DirectBuffer;
import org.junit.Test;

public class EvaluationContextTest {

  private final ExpressionLanguage expressionLanguage =
      ExpressionLanguageFactory.createExpressionLanguage();

  @Test
  public void stringVariable() {
    final var variable = asMsgPack("\"y\"");
    final var evaluationResult = evaluateExpressionWithContext(variable);

    assertThat(evaluationResult.getType()).isEqualTo(ResultType.STRING);
    assertThat(evaluationResult.getString()).isEqualTo("y");
    assertThat(evaluationResult.toBuffer()).isEqualTo(variable);
  }

  @Test
  public void numericVariable() {
    final var variable = asMsgPack("1");
    final var evaluationResult = evaluateExpressionWithContext(variable);

    assertThat(evaluationResult.getType()).isEqualTo(ResultType.NUMBER);
    assertThat(evaluationResult.getNumber()).isEqualTo(1L);
    assertThat(evaluationResult.toBuffer()).isEqualTo(variable);
  }

  @Test
  public void booleanVariable() {
    final var variable = asMsgPack("true");
    final var evaluationResult = evaluateExpressionWithContext(variable);

    assertThat(evaluationResult.getType()).isEqualTo(ResultType.BOOLEAN);
    assertThat(evaluationResult.getBoolean()).isTrue();
    assertThat(evaluationResult.toBuffer()).isEqualTo(variable);
  }

  @Test
  public void nullVariable() {
    final var variable = asMsgPack("null");
    final var evaluationResult = evaluateExpressionWithContext(variable);

    assertThat(evaluationResult.getType()).isEqualTo(ResultType.NULL);
    assertThat(evaluationResult.toBuffer()).isEqualTo(variable);
  }

  @Test
  public void listVariable() {
    final var variable = asMsgPack("[1,2,3]");
    final var evaluationResult = evaluateExpressionWithContext(variable);

    assertThat(evaluationResult.getType()).isEqualTo(ResultType.ARRAY);
    assertThat(evaluationResult.toBuffer()).isEqualTo(variable);
  }

  @Test
  public void contextVariable() {
    final var variable = asMsgPack(Map.of("y", 1));
    final var evaluationResult = evaluateExpressionWithContext(variable);

    assertThat(evaluationResult.getType()).isEqualTo(ResultType.OBJECT);
    assertThat(evaluationResult.toBuffer()).isEqualTo(variable);
  }

  @Test
  public void nestedContextVariable() {
    final var variable = asMsgPack(Map.of("y", Map.of("z", 1)));
    final var evaluationResult = evaluateExpressionWithContext(variable);

    assertThat(evaluationResult.getType()).isEqualTo(ResultType.OBJECT);
    assertThat(evaluationResult.toBuffer()).isEqualTo(variable);
  }

  @Test
  public void listOfListsVariable() {
    final var variable = asMsgPack("[[1],[2],[3]]");
    final var evaluationResult = evaluateExpressionWithContext(variable);

    assertThat(evaluationResult.getType()).isEqualTo(ResultType.ARRAY);
    assertThat(evaluationResult.toBuffer()).isEqualTo(variable);
  }

  @Test
  public void listOfContextsVariable() {
    final var variable = asMsgPack("[{\"y\":1}, {\"z\":2}]");
    final var evaluationResult = evaluateExpressionWithContext(variable);

    assertThat(evaluationResult.getType()).isEqualTo(ResultType.ARRAY);
    assertThat(evaluationResult.toBuffer()).isEqualTo(variable);
  }

  private EvaluationResult evaluateExpressionWithContext(final DirectBuffer variable) {
    final var parseExpression = expressionLanguage.parseExpression("=x");
    final var evaluationResult =
        expressionLanguage.evaluateExpression(
            parseExpression, StaticEvaluationContext.of(Map.of("x", variable)));

    assertThat(evaluationResult.isFailure())
        .describedAs(evaluationResult.getFailureMessage())
        .isFalse();

    return evaluationResult;
  }
}