/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.model.traits;

import java.util.List;
import java.util.Optional;
import software.amazon.smithy.model.SourceException;
import software.amazon.smithy.model.node.ArrayNode;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.utils.MapUtils;
import software.amazon.smithy.utils.ToSmithyBuilder;

/**
 * Defines values which are specifically allowed and/or disallowed.
 */
public final class TestVectorsTrait extends AbstractTrait implements ToSmithyBuilder<TestVectorsTrait> {
    public static final ShapeId ID = ShapeId.from("smithy.api#testVectors");

    private final List<Node> allowed;
    private final List<Node> disallowed;

    private TestVectorsTrait(TestVectorsTrait.Builder builder) {
        super(ID, builder.sourceLocation);
        this.allowed = builder.allowed;
        this.disallowed = builder.disallowed;
        if (allowed == null && disallowed == null) {
            throw new SourceException("One of 'allowed' or 'disallowed' must be provided.", getSourceLocation());
        }
        if (allowed != null && allowed.isEmpty()) {
            throw new SourceException("'allowed' must be non-empty when provided.", getSourceLocation());
        }
        if (disallowed != null && disallowed.isEmpty()) {
            throw new SourceException("'disallowed' must be non-empty when provided.", getSourceLocation());
        }
    }

    /**
     * Gets the allowed values.
     *
     * @return returns the optional allowed values.
     */
    public Optional<List<Node>> getAllowed() {
        return Optional.ofNullable(allowed);
    }

    /**
     * Gets the disallowed values.
     *
     * @return returns the optional disallowed values.
     */
    public Optional<List<Node>> getDisallowed() {
        return Optional.ofNullable(disallowed);
    }

    @Override
    protected Node createNode() {
        return new ObjectNode(MapUtils.of(), getSourceLocation())
                .withOptionalMember("allowed", getAllowed().map(ArrayNode::fromNodes))
                .withOptionalMember("disallowed", getDisallowed().map(ArrayNode::fromNodes));
    }

    @Override
    public TestVectorsTrait.Builder toBuilder() {
        return builder().allowed(allowed).disallowed(disallowed).sourceLocation(getSourceLocation());
    }

    /**
     * @return Returns a new TestVectorsTrait builder.
     */
    public static TestVectorsTrait.Builder builder() {
        return new TestVectorsTrait.Builder();
    }

    /**
     * Builder used to create a TestVectorsTrait.
     */
    public static final class Builder extends AbstractTraitBuilder<TestVectorsTrait, TestVectorsTrait.Builder> {
        private List<Node> allowed;
        private List<Node> disallowed;

        public TestVectorsTrait.Builder allowed(List<Node> allowed) {
            this.allowed = allowed;
            return this;
        }

        public TestVectorsTrait.Builder disallowed(List<Node> disallowed) {
            this.disallowed = disallowed;
            return this;
        }

        @Override
        public TestVectorsTrait build() {
            return new TestVectorsTrait(this);
        }
    }

    public static final class Provider implements TraitService {
        @Override
        public ShapeId getShapeId() {
            return ID;
        }

        @Override
        public TestVectorsTrait createTrait(ShapeId target, Node value) {
            TestVectorsTrait.Builder builder = builder().sourceLocation(value.getSourceLocation());
            value.expectObjectNode()
                    .getMember("allowed", TestVectorsTrait.Provider::convertToTestVectorList, builder::allowed)
                    .getMember("disallowed", TestVectorsTrait.Provider::convertToTestVectorList, builder::disallowed);
            TestVectorsTrait result = builder.build();
            result.setNodeCache(value);
            return result;
        }

        private static List<Node> convertToTestVectorList(Node node) {
            return node.expectArrayNode().getElements();
        }
    }
}
