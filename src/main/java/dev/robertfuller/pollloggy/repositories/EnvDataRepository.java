package dev.robertfuller.pollloggy.repositories;

import dev.robertfuller.pollloggy.models.EnvDataRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EnvDataRepository extends MongoRepository<EnvDataRecord, String> {
}
