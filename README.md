# Event Bus Annotation Processing.

1. There can be multiple listeners for one event
2. for the event subscriber, the event must be the first object of the method
3. Object must be annotated with @AggregateEvent 
4. must be a singleton bean

- first implementation
  - subscriber must accept a single param of an event type
  - class must be annotated with @Singleton bean as well as @EventListener