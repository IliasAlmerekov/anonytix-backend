@org.springframework.modulith.ApplicationModule(
        displayName = "Company",
        allowedDependencies = {
            "shared::domain",
            "shared::error",
            "shared::mapper"
        })
package de.anonytix.company;
