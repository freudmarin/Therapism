package com.marindulja.mentalhealthbackend.dtos.mapping;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ModelMappingUtility {

    private final ModelMapper modelMapper;

    public ModelMappingUtility(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }


    // Generic method to map from source to target type
    public <S, T> T map(S source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }

    // Optional: Add more utility methods for common mapping tasks if needed
}
