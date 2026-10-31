package dev.robertfuller.pollloggy;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

public class PollLoggyApplicationUnitTests {
    @Test
    public void testMainDelegatesToSpringApplication () {
        String[] args = {"--foo=bar"};
        try (MockedStatic<SpringApplication> spring = mockStatic(SpringApplication.class)) {
            PollLoggyApplication.main(args);
            spring.verify(() -> SpringApplication.run(PollLoggyApplication.class, args));
        }
    }
}
