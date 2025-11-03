/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.validation.validators;

import java.util.*;
import java.util.stream.Collectors;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.internal.NodeHandler;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.traits.TestVectorsTrait;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;

/**
 * Emits a validation event if a test vector conflicts with their configured validation traits.
 */
public final class TestVectorsTraitValidator extends AbstractValidator {

    @Override
    public List<ValidationEvent> validate(Model model) {
        List<ValidationEvent> events = new ArrayList<>();
        for (Shape shape : model.getShapesWithTrait(TestVectorsTrait.class)) {
            validateTestValuesTrait(events, model, shape);
        }

        return events;
    }

    private void validateTestValuesTrait(List<ValidationEvent> events, Model model, Shape shape) {
        TestVectorsTrait trait = shape.expectTrait(TestVectorsTrait.class);

        List<Node> allowedValueNodes = trait.getAllowed().orElseGet(Collections::emptyList);
        for (int index = 0; index < allowedValueNodes.size(); index += 1) {
            Node allowedValueNode = allowedValueNodes.get(index);

            NodeValidationVisitor visitor = NodeValidationVisitor.builder()
                    .model(model)
                    .eventId(getName() + ".allowed." + index)
                    .eventShapeId(shape.getId())
                    .value(allowedValueNode)
                    .startingContext(String.format("The allowed test vector `%s` failed a validation", NodeHandler.print(allowedValueNode)))
                    .build();

            List<ValidationEvent> errorValidationEvents = shape.accept(visitor)
                    .stream()
                    .filter(validationEvent -> validationEvent.getSeverity() == Severity.ERROR)
                    .collect(Collectors.toList());

            events.addAll(errorValidationEvents);
        }

        List<Node> disallowedValueNodes = trait.getDisallowed().orElseGet(Collections::emptyList);
        for (int index = 0; index < disallowedValueNodes.size(); index += 1) {
            Node disallowedValueNode = disallowedValueNodes.get(index);

            NodeValidationVisitor visitor = NodeValidationVisitor.builder()
                    .model(model)
                    .value(disallowedValueNode)
                    .build();

            List<ValidationEvent> errorValidationEvents = shape.accept(visitor)
                    .stream()
                    .filter(validationEvent -> validationEvent.getSeverity() == Severity.ERROR)
                    .collect(Collectors.toList());

            if (errorValidationEvents.isEmpty()) {
                events.add(error(shape,
                        disallowedValueNode,
                        String.format("The disallowed test vector `%s` passed all validations when it shouldn't have",
                                NodeHandler.print(disallowedValueNode)),
                        "disallowed",
                        Integer.toString(index)));
            }
        }
    }
}
