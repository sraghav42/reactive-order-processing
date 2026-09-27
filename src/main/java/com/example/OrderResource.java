package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/api/orders")
@ApplicationScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderResource {

    @Channel("order-events")
    Emitter<Order> orderEvents;

    @POST
    @Transactional
    public Response create(@Valid CreateOrderRequest request, @Context UriInfo uriInfo) {
        Order order = new Order();
        order.item = request.item;
        order.quantity = request.quantity;
        order.customerId = request.customerId;
        order.status = Order.PENDING;
        order.persist();

        Order responseOrder = new Order();
        responseOrder.id = order.id;
        responseOrder.item = order.item;
        responseOrder.quantity = order.quantity;
        responseOrder.customerId = order.customerId;
        responseOrder.status = order.status;

        orderEvents.send(order);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(order.id.toString()).build())
                .entity(responseOrder)
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        Order order = Order.findById(id);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        return Response.ok(order).build();
    }
}
