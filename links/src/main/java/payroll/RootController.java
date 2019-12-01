package payroll;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RootController {

	@GetMapping
	RepresentationModel index() {
		RepresentationModel rootRepresentationModel = new RepresentationModel();
		rootRepresentationModel.add(linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
		rootRepresentationModel.add(linkTo(methodOn(OrderController.class).all()).withRel("orders"));
		return rootRepresentationModel;
	}

}
