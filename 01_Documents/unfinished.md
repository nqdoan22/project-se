### Batch components

- Allow Production role user to add components to production batch
- Each component is a quantity of a specific material taken from a specific inventory lot. The unit of measure must be the same as the unit used for that lot.
- A production batch can have any number of components.
- Each component must use an amount that is available in the inventory: the amount specified must be nno more than the amount left of the inventory lot. Take into account past inventory transactions for that lot, as well as any other pending production batches using that lot.
- While the production batch is pending, user may update its components, add more or remove some. After that point, it may no longer be changed
- When the production batch is in manufacturing (user indicates status is no longer pending), record inventory transactions in the database by subtracting the specified quantity from each inventory lot

*See dbscript.sql for database schema for BatchComponents

#### Tasks

- Backend
    - Add entity and repository for batch components - exists
    - Create controller to handle routes for batch components: create, update, delete, view for specific production batch, view all (grouped by batch) - OK
    - Update ProductionBatchController route that handles status changes to write inventory transactions based on its batch components - 
- Frontend
    - Add batch components tab - OK
        - Can filter by production batch - OK
        - Optional: can filter by material - OK
        - Modal to add new / update component - OK
            - Show error if the user tries to use a quantity more than available
    - In the production batch tab, in the details modal for a batch, show current batch components
        - Add button to redirect to add new component (set production batch ID to currently viewed batch)


### Inventory transactions

- Allow InventoryManager role user to add transactions to inventory lots (not tied to production batches)
- A transaction is tied to a specific inventory lot, and can have positive quantity for adding back to lot, or negative quantity for taking from lot
- If taking from the lot, the quantity must not be more than the lot's remaining quantity. This quantity is calculated as `base + transactions - pending`, where `base` is the lot's initial quantity as recorded in InventoryLots, `transactions` is the total of all transactions involving this lot, and `pending` is the total quantity to be used in pending production batches, as recorded in BatchComponents


## Tasks

- Backend
    - Add entity and repository for inventory transactions
    - Create controller handle routes for inventory transactions: create, update, view all, view with filters (material / inventory lot / date range)
    -  Do not allow updates of transaction quantity (including unit of measure) or lot used. Only allow updates for transaction type, notes, and trasaction author (performed_by). Do not allow deletes. If transaction quantities need to be changed, a new transaction must be created with the type Adjustment.
- Frontend
    - Add inventory transactions tab
        - Can filter by material / inventory lot / date range (example: last 24h, last 30 days, last year, all time)
        - Modal to add a new transaction
            - Show error if the user tries to use a quantity more than available
        - Modal to update transaction (only type, notes and author)
    - Add inventory transactions stats to report tab


### Other changes / bug fixes

- When a production batch is marked Complete, automatically add a new inventory lot of this material to the database. Use quantity and material information of the production batch. If it is rejected, do not add the inventory lot.