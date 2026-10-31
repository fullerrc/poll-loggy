package dev.robertfuller.pollloggy;

import org.springframework.boot.SpringApplication;

public class TestPollLoggyApplication {

    public static void main(String[] args) {
        SpringApplication.from(PollLoggyApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
