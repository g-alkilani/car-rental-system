Points to consider:
1) Some code is duplicated, so base implementations may be a good idea(essentially, template pattern).
2) Iterating over bookings to see whether there is an available car for a requested period
has room for improvement. For instance, bookings can be kept per each carId and even sorted by startDate.
If bookings are limited in length, then many of them can be eliminated from scanning. A tradeoff is that 
the logic and structure will be more complex, plus adding bookings will take longer. So, this is worth considering if performance presents an issue.
3) In the thread-safe version, a more fine-grained locking is possible at the expense of internals' complexity.
4) In a real-life app, we will have to rely on DB as a source of truth. The main problem to address will be to avoid overbooking.
I can suggest multiple approaches:
  - using some form of pessimistic locking, like 'select for update';
  - running transactions to reserve cars at Serializable level to avoid write skews; lower levels may not be sufficient
  - relying on composite primary keys: carId + time slot; granularity is essential here
  - using a built-in range type like 'tsrange' in Postgres

All of them come with tradeoffs, so it is worth spending time on looking at them carefully.
