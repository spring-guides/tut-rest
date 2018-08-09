package payroll;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// tag::main[]
@RestController
class OrderController {

	private final OrderRepository orderRepository;
	private final OrderResourceAssembler assembler;

	OrderController(OrderRepository orderRepository,
					OrderResourceAssembler assembler) {

		this.orderRepository = orderRepository;
		this.assembler = assembler;
	}

	@GetMapping("/orders")
	Resources<Resource<Order>> all() {

		List<Resource<Order>> orders = orderRepository.findAll().stream()
			.map(assembler::toResource)
			.collect(Collectors.toList());

		return new Resources<>(orders,
			linkTo(methodOn(OrderController.class).all()).withSelfRel());
	}

	@GetMapping("/orders/{id}")
	Resource<Order> one(@PathVariable Long id) {
		return assembler.toResource(
			orderRepository.findById(id)
				.orElseThrow(() -> new OrderNotFoundException(id)));
	}

	@PostMapping("/orders")
	ResponseEntity<Resource<Order>> newOrder(@RequestBody Order order) {

		order.setStatus(Status.IN_PROGRESS);
		Order newOrder = orderRepository.save(order);

		return ResponseEntity
			.created(linkTo(methodOn(OrderController.class).one(newOrder.getId())).toUri())
			.body(assembler.toResource(newOrder));
	}
	// end::main[]
	
	// tag::delete[]
	@DeleteMapping("/orders/{id}/cancel")
	ResponseEntity<ResourceSupport> cancel(@PathVariable Long id) {

		Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

		if (order.getStatus() == Status.IN_PROGRESS) {
			order.setStatus(Status.CANCELLED);
			return ResponseEntity.ok(assembler.toResource(orderRepository.save(order)));
		}

		return ResponseEntity
			.status(HttpStatus.METHOD_NOT_ALLOWED)
			.body(new VndErrors.VndError("Method not allowed", "You can't cancel an order that is in the " + order.getStatus() + " status"));
	}
	// end::delete[]

	// tag::complete[]
	@PutMapping("/orders/{id}/complete")
	ResponseEntity<ResourceSupport> complete(@PathVariable Long id) {
		
			Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));

			if (order.getStatus() == Status.IN_PROGRESS) {
				order.setStatus(Status.COMPLETED);
				return ResponseEntity.ok(assembler.toResource(orderRepository.save(order)));
			}

			return ResponseEntity
				.status(HttpStatus.METHOD_NOT_ALLOWED)
				.body(new VndErrors.VndError("Method not allowed", "You can't complete an order that is in the " + order.getStatus() + " status"));
	}
	// end::complete[]
}
