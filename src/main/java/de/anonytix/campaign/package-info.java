@org.springframework.modulith.ApplicationModule(
        displayName = "Campaign",
        allowedDependencies = {
            "company",
            "survey",
            "shared::domain",
            "shared::error",
            "shared::mapper"
        })
package de.anonytix.campaign;
