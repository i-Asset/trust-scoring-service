package nimble.trust.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
   
	@Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select().apis(RequestHandlerSelectors.basePackage("nimble.trust"))
                .build().apiInfo(apiInfo());
    }
	
	private ApiInfo apiInfo() {
	     return new ApiInfo(
	       "Nimble Trust Scoring and Ranking REST API", 
	       "REST API NIMBLE TRM", 
	       "1.0", "Terms of service", 
	       new springfox.documentation.service.Contact("", "https://www.nimble-project.org/", "m.vujasinovic@innova-eu.net"), 
	       "Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0");
	}
}
