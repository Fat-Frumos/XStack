package com.epam.esm.gym.web.provider.trainer;

import com.epam.esm.gym.domain.Specialization;
import com.epam.esm.gym.domain.TrainingType;
import com.epam.esm.gym.dto.trainer.TrainerProfile;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.http.ResponseEntity;

import java.util.stream.Stream;

public class TrainerProfileArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
        TrainingType transfiguration = TrainingType.builder()
                .trainingType(Specialization.TRANSFIGURATION)
                .build();

        TrainerProfile profile = TrainerProfile.builder()
                .username("Minerva.McGonagall")
                .firstName("Minerva")
                .lastName("McGonagall")
                .specialization(transfiguration)
                .active(true)
                .build();

        return Stream.of(
                Arguments.of("Minerva.McGonagall", ResponseEntity.ok(profile))
        );
    }
}
