package hello;

import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.parser.JSONParser;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
public class HelloController {

	/* Storing JSON Data into Redis */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{uriType}", method = RequestMethod.POST, consumes = "application/json")
	public ResponseEntity<Object> createSchema(@RequestHeader HttpHeaders headers, @RequestBody String entity)
			throws Exception {

		File schemaFile = new File("C:\\Users\\Siddhant Chandiwal\\Documents\\workspace-sts-3.9.2.RELEASE\\gs-spring-boot-initial\\src\\main\\resources\\final-json-schema.json");
		String str = FileUtils.readFileToString(schemaFile);

		try {
			System.out.println("Validating Schema now " + Utils.isJsonValid(str, entity));
			if (Utils.isJsonValid(str, entity)) {
				System.out.println("Valid!");
				Jedis jedis = new Jedis("127.0.0.1", 6379);

				UUID uuid = UUID.randomUUID();
				String taskId = uuid.toString();
				System.out.println("Task Id " + taskId);
				try {
					ObjectMapper mapper = new ObjectMapper();
					Map<String, Object> map = new HashMap<String, Object>();
					map = mapper.readValue(entity, new TypeReference<Map<String, Object>>() {
					});
					jedis.set("Plan-" + taskId, map.toString());
					// jedis.set("Try", taskId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				HttpHeaders httpHeaders = new HttpHeaders();
				return new ResponseEntity("Object Stored with ID: Plan-" + taskId, httpHeaders,
						org.springframework.http.HttpStatus.CREATED);

			} else {
				System.out.println("NOT valid!");
				return new ResponseEntity("Invalid JSON Data", org.springframework.http.HttpStatus.BAD_REQUEST);
			}
		} catch (ProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// display entire data
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Object> get(@RequestHeader HttpHeaders headers, @PathVariable String id) throws Exception {

		Jedis jedis = new Jedis("127.0.0.1", 6379);
		System.out.println("ID Value is " + id);
		String result = jedis.get(id);
		if (result != null) {
			System.out.println("Result Value is " + result);
			HttpHeaders httpHeaders = new HttpHeaders();
			return new ResponseEntity(result, httpHeaders, org.springframework.http.HttpStatus.OK);
		} else {
			return new ResponseEntity("Requested Data not Found", org.springframework.http.HttpStatus.NOT_FOUND);
		}
	}

	// delete id
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = "application/json")
	public ResponseEntity<Object> del(@RequestHeader HttpHeaders headers, @PathVariable String id) throws Exception {

		JSONParser parser = new JSONParser();
		Jedis jedis = new Jedis("127.0.0.1", 6379);
		if(jedis.exists(id)) {
			System.out.println("Id exists in database");
			Long result = jedis.del(id);
			HttpHeaders httpHeaders = new HttpHeaders();
			System.out.println("Deleting the Object now");
			return new ResponseEntity("Requested Task is now deleted", httpHeaders,	org.springframework.http.HttpStatus.OK);
		}else {
			System.out.println("ID does not exists in database");
			return new ResponseEntity("ID not found in database", org.springframework.http.HttpStatus.NOT_FOUND);
		}	     	
	}
	
}
