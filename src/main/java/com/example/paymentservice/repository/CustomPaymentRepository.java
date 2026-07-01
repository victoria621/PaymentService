    package com.example.paymentservice.repository;

    import com.example.paymentservice.dto.SumResult;
    import com.example.paymentservice.entity.PaymentEntity;
    import com.example.paymentservice.entity.PaymentStatus;
    import org.springframework.data.mongodb.core.MongoTemplate;
    import org.springframework.data.mongodb.core.aggregation.Aggregation;
    import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
    import org.springframework.data.mongodb.core.aggregation.AggregationResults;
    import org.springframework.data.mongodb.core.query.Criteria;
    import org.springframework.data.mongodb.core.query.Query;
    import org.springframework.stereotype.Component;

    import java.math.BigDecimal;
    import java.time.Instant;
    import java.util.ArrayList;
    import java.util.List;

    @Component
    public class CustomPaymentRepository {

        private final MongoTemplate mongoTemplate;

        public CustomPaymentRepository(MongoTemplate mongoTemplate) {
            this.mongoTemplate = mongoTemplate;
        }

        public List<PaymentEntity> findPaymentsByCriteria(Long userId, Long orderId, PaymentStatus status){
            Query query = new Query();
            List<Criteria> criteriaList = new ArrayList<>();

            if(userId != null){
                criteriaList.add(Criteria.where("userId").is(userId));
            }
            if(orderId != null){
                criteriaList.add(Criteria.where("orderId").is(orderId));
            }
            if(status != null ){
                criteriaList.add(Criteria.where("status").is(status));
            }

            if (!criteriaList.isEmpty()) {
                query.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
            }

            return mongoTemplate.find(query, PaymentEntity.class);
        }

        public BigDecimal sumPaymentsByCriteria(Long userId, Instant start, Instant end) {
            List<AggregationOperation> operations = new ArrayList<>();
            Criteria criteria = new Criteria();

            if (userId != null) {
                criteria.and("userId").is(userId);
            }
            if (start != null && end != null) {
                criteria.and("timestamp").gte(start).lte(end);
            }

            if (!criteria.getCriteriaObject().isEmpty()) {
                operations.add(Aggregation.match(criteria));
            }
            operations.add(Aggregation.group()
                    .sum("paymentAmount").as("total"));

            Aggregation aggregation = Aggregation.newAggregation(operations);
            AggregationResults<SumResult> results = mongoTemplate.aggregate(
                    aggregation,
                    PaymentEntity.class,
                    SumResult.class
            );

            if (results.getUniqueMappedResult() != null) {
                return results.getUniqueMappedResult().getTotal();
            }
            return BigDecimal.ZERO;
        }

    }
