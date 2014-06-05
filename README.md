# Microservice-oriented Architectures 

It's taken a while, but we've finally agreed upon a name for _that which we all sort of knew, anyway_ - lo, _microservices_!  [_Microservices_](http://martinfowler.com/articles/microservices.html),   describe an architectural style that promotes _singly focused_ services as the basis for sophisticated systems. _It's SOA, but with REST_. At least that's the tagline.  

Communication doesn't _have_ to be REST, it might be done over something messaging-centric, like [AMQP](http://rabbitmq.org) or [0MQ](). 

Why REST? [_REST In Practice_](http://www.amazon.com/gp/product/0596805829?ie=UTF8&tag=martinfowlerc-20&linkCode=as2&camp=1789&creative=9325&creativeASIN=0596805829)  proffers, [to borrow Martin Fowler's phrasing](),  "the notion that the web is an existence proof of a massively scalable distributed system that works really well, and we can take ideas from that to build integrated systems more easily." I think that's a pretty good reason: REST embraces the precepts of the web itself, and embraces its architecture, benefits and all.  

What benefits? ...

That still leaves open the question of service granularity. Just how big (or _small_) is a *micro* service? [Chris Richardson](http://twitter.com/crichardson) pointed me to a great talk by [Fred Georges](), out of the UK. Fred and his team sought to grapple with the same question and ultimately the  team decided upon _about 100 lines-of-code_. That's a fairly small number. It's small enough, suggests Fred, that if a service is better expressed in another language, then one should feel no fear in throwing away the existing one and simply rewriting it, _in an afternoon_. It's so small that one could easily find an error, decide it'd be simpler to rewrite the service, and then do so all in the time allotted to debug the existing one. It's so small that the system itself becomes very, very maleable. 

As Richardson explains, a functioning application is anything but: instead what we think of as applications today are in actuality typically a connected system of services. There is no single `.jar` or `.war` that you can deploy to get Google.com on your local machine. Services that can be scaled out according to the requirements. Services that can manage only one slice of the pie. 

So it's going to be smaller than not.  RedHat's Mark Little expands upon this idea in his post, [_Nano Services_](http://www.infoq.com/news/2014/05/nano-services). From the article, "Arnon's point is that services with 10 to 100 lines are likely to be exposing functions rather than being a 'real service.' He also believes that the the smaller the service gets (towards what he calls 'Nano services') the more mangement overhead you have to worry about, serialization/deserialization costs, security etc. Essentially the smaller these services become, the more glue you need to pull them together into a useful 'whole.'" 

Little's post suggests that it's hard to imagine useful services at only a 100 LOC. Certainly, such services would need to take for granted common connectivity. monitoring and management concerns. Such services would need to 

So we want services that justify (and tow!) their own capacitive weight, so to speak, and we want services to be singly focused, in the UNIX command-line tool tradition. 

That's _so_ small in fact that one wonders just what's being talked about. What can you get done in your language and framework of choice in 100 LOCs? If you take for granted all the wiring, installation and integration that each service must support to be mesh together as part of a larger system, then you're really just talking about a  [ smart service-host](http://arnon.me/soa-patterns/service-host/). 


A _smart service host_ is code, I think, for something like a Platform-as-a-Service. If you look at the way most of the big architectures today, they invariably look something like microservices. There's usually a layer of scripting around the edges that handles job coordination, rebalancing according to service demands, and more. This layer of scripting is _very_ hard to get right and tends to work at the whim of the underlying provider, like Amazon Web Services. Some organizations got smart, and outsourced and open-sourced this infrastructural layer. Google, first, with its Google App Engine. Heroku, Engineyard, and more followed shortly after. There's no reason today to every write this infrastructure layer - it's commodity, now, thanks to the likes of projects like [Cloud Foundry](http://cloudfoundry.org). 

I'm not even sure if 100 LOC is a useful metric, even then, but it _does_ give us something to aim for: smaller is better! 

In a microservice architecture, services are exposed to handle a single domain. Suppos you were trying to build a blog _application_. Such a blog might consist of a module that handles blog content creation and persistence (persisting, perhaps, to a document-store like Couchbase or MongoDB), a module to handle tag parsing and graphing (persiting, perhaps, to a Graph database which can model word stems, aliases, etc.), a module to handle uploaded images (including persistence to a blob store like S3), a module to handle the on-site chat powerd by websockets delegating to something like RabbitMQ, etc. 

Indeed, each of these services might persist their state in a different backend. Some might benefit from horizontal scaling as load increases. The state requiremnts for each are different and so too are the solutions: it's hard to look at any of those scenarios and recommend an RDBMS as _the best_ choice, if we're honest.  

This of course invites _yet another_ question: how do services synchronize state?  It's all too easy at this point to look to distributed transactions, and ACID. How do we ensure transactional state between services?   

[Jan Stenberg investigates this very point in the  post, _Microservices: Usage is More Important than Size_](http://www.infoq.com/news/2014/05/microservices-usage-size). One approach originally proferred by [Jeppe Cramon](http://www.cramon.dk/Cramon/Front_page.html), a Danish SOA-ninja, offers that a solution is 3-pronged:

* it must allocate data to services 
* it must identify data
* it must enable communication between services

The [Domain-Driven Design (DDD)](http://en.wikipedia.org/wiki/Domain-driven_design) concept has  data collected into _entities_ and _aggregates_  where each aggregate is uniquely identifiable using e.g. a UUID, and allocated to one service. This handles the first two points: aggregates must be consistent after a transaction, the rule of thumb being: 1 use case = 1 transaction = 1 aggregate. So, you don't have data that overlays two services. Systems keep pointers to each other through UUIDs, or references. Data should not overlay multiple services. This sidesteps the transaction issue by sidestepping shared state altogether. Going back to our blog example, the service that handles tagging needs only to ensure that the state of persistent tags is valid, nothing more.  

Think about the use cases of working with tags: you type them out, and - hopefully - the UI persists them on focus change. If it's done correctly, you'll see the validation and confirmation of your tags' state, _as you're typing them_! Imagine, for example, a little Ajax





# The Role of Agile Infrastructure in a Service Driven Architecture 
Designing for a service-oriented architecture implies that you'll have the infrastructure to support these services. Services - especially web services - can benefit from common infrastructure like federated sign-on (single sign-on),  routing, and service provisioning. Happily, a  good Platform-as-a-Service cloud like [Cloud Foundry](http://run.pivotal.io) or [Heroku](http://heroku.com) provides most such infrastructure and we can plugin the rest with Spring Boot.  A PaaS provides some of the benefit of a smart service host.

http://www.infoq.com/news/2014/05/microservices-usage-size 

# Talk to Me
Clients can communicate with a microservice via any easy, ubiquitous 
Ask yourself how you expect clients to communicate with your microservice. These days, most services are exposed using REST. though they could as easily be through some other protocol or technology, like message queues like [RabbitMQ](http://rabbitmq.org). When we talk about transport today, as often as not we're talkig 

# Building a REST service 

# Building a HATEOAS REST Service


# Securing a REST Service with OAuth 
TODO: talk about storing Access Tokens in a single store that multiple client nodes use for federated ID in mind. Then show how you can use Spring Boot's autoconfiguration approach to share across an organization's services w/ no configuration apart from `@EnableAutoConfiguration`. Show an example of Spring Security OAuth persiting to Redis or something more horizontally scalable than a SQL DB.  

# Consuming an OAuth REST Service 

# Using Spring Integration to expose, forward-and-store, and delegate to services w/ RabbitMQ

# Deploying a MicroService 
Automating AB deployments. Automating database management. Automating rabbbitmq deployment and adding a new service. 

# Microservice Health Checks 

Look at Rob harrops' SpringOne talk on using AWS and his discussion of how to create AWS instances using the AWS console and then use the Elastic Load Balancer to check a node's health using a Spring Boot /health endpoint. Use AWS autoscaling groups.

Then show how to do the same sort of thing using Cloud Foundry

Show how to tie Cloud Foundry availability to the node's `/heatlh` endpoint.  


# A Microservice by any other name

# 
