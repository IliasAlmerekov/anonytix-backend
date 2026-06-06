@org.springframework.modulith.ApplicationModule(
        displayName = "Feedback",
        allowedDependencies = {
            "campaign",
            "company",
            "survey",
            "shared::error"
        })
package de.anonytix.feedback;
