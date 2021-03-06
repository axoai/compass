package org.zalando.compass.features;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.zalando.compass.Application;
import org.zalando.compass.library.FixedClockConfiguration;
import org.zuchini.junit.ZuchiniOptions;
import org.zuchini.spring.SpringZuchini;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringZuchini.class)
@ZuchiniOptions(
        featurePackages = "features",
        stepDefinitionPackages = "org.zalando.compass.features",
        reportIndividualSteps = true
)
@SpringBootTest(
        webEnvironment = DEFINED_PORT,
        classes = {
                Application.class,
                FixedClockConfiguration.class,
        },
        properties = "spring.main.allow-bean-definition-overriding: true")
@ActiveProfiles("test")
public class FeatureComponentTest {

}
