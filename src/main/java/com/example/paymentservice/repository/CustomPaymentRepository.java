    package com.example.paymentservice.repository;

    import com.example.paymentservice.entity.PaymentEntity;
    import org.springframework.data.mongodb.core.MongoTemplate;
    import org.springframework.data.mongodb.core.query.Criteria;
    import org.springframework.data.mongodb.core.query.Query;
    import org.springframework.stereotype.Component;

    import java.util.ArrayList;
    import java.util.List;

    @Component
    public class CustomPaymentRepository {

        private final MongoTemplate mongoTemplate;

        public CustomPaymentRepository(MongoTemplate mongoTemplate) {
            this.mongoTemplate = mongoTemplate;
        }

        public List<PaymentEntity> findPaymentsByCriteria(Long userId, String orderId, String status){
            Query query = new Query();
            List<Criteria> criteriaList = new ArrayList<>();

            if(userId != null){
                criteriaList.add(Criteria.where("userId").is(userId));
            }
            if(orderId != null){
                criteriaList.add(Criteria.where("orderId").is(orderId));
            }
            if(status != null && !status.isEmpty()){
                criteriaList.add(Criteria.where("status").is(status));
            }

            if (!criteriaList.isEmpty()) {
                query.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
            }

            return mongoTemplate.find(query, PaymentEntity.class);
        }

    }
