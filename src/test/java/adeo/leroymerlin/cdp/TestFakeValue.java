package adeo.leroymerlin.cdp;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;


public class TestFakeValue {
	@Test
	public void generateRandomEvent(){
		Faker faker = new Faker();
		String cityName = faker.music().genre() + " in "+ faker.address().cityName();
System.out.println(cityName);
	}
}
