package payroll;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
class OrderModelAssembler implements RepresentationModelAssembler<Order, EntityModel<Order>> {

	@Override
	public EntityModel<Order> toModel(Order order) {

		// Unconditional links to single-item entityModel and aggregate root

		EntityModel<Order> orderEntityModel = new EntityModel<>(order,
			linkTo(methodOn(OrderController.class).one(order.getId())).withSelfRel(),
			linkTo(methodOn(OrderController.class).all()).withRel("orders")
		);

		// Conditional links based on state of the order
		
		if (order.getStatus() == Status.IN_PROGRESS) {
			orderEntityModel.add(
				linkTo(methodOn(OrderController.class)
					.cancel(order.getId())).withRel("cancel"));
			orderEntityModel.add(
				linkTo(methodOn(OrderController.class)
					.complete(order.getId())).withRel("complete"));
		}

		return orderEntityModel;
	}
}
