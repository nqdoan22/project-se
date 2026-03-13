### Batch components

- Allow Production role user to add components to production batch
- Each component is a quantity of a specific material taken from a specific inventory lot.
- A production batch can have any number of components.
- Each component must use an amount that is available in the inventory: the amount specified must be nno more than the amount left of the inventory lot. Take into account past inventory transactions for that lot, as well as any other pending production batches using that lot.
- While the production batch is pending, user may update its components, add more or remove some. After that point, it may no longer be changed
- When the production batch is in manufacturing (user indicates status is no longer pending), record inventory transactions in the database by subtracting the specified quantity from each inventory lot
- If the production batch is marked Complete, automatically add a new inventory lot of this material to the database. Use quantity and material information of the production batch. If it is rejected, do not add the inventory lot.

*See dbscript.sql for database schema for BatchComponents


### Inventory transactions

- Allow InventoryManager role user to add transactions to inventory lots (not tied to production batches)
- A transaction is tied to a specific inventory lot, and can have positive quantity for adding back to lot, or negative quantity for taking from lot
- If taking from the lot, the quantity must not be more than the lot's remaining quantity. This quantity is calculated as `base + transactions - pending`, where `base` is the lot's initial quantity as recorded in InventoryLots, `transactions` is the total of all transactions involving this lot, and `pending` is the total quantity to be used in pending production batches, as recorded in BatchComponents