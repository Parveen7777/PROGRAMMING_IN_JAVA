# CO1 Java Assignment 1
## Campus Smart-Parking System

**Student Name:** [Enter your name]  
**Register Number:** [Enter your register number]  
**GitHub Link:** [Paste your repository link]  
**Java Version:** JDK 8 or later

> This is a simple student-level explanation. Read the answers, run the programs, and make sure you can explain them during the viva.

---

## Task 1 – Understand the Problem and Identify the Objects

**Inputs:** Vehicle registration number, vehicle type, user type, entry time, exit time and available parking slots.

**Outputs:** Allocated floor and slot, ticket number, parking duration, parking fee and slot availability.

| Candidate class | Responsibility |
|---|---|
| Vehicle | Stores registration number and vehicle type. |
| ParkingSlot | Stores floor number, slot number and whether it is occupied. |
| ParkingTicket | Stores ticket number, vehicle, entry time and fee. |
| ParkingFacility | Manages floors, slots, vehicle entry and exit. |
| User | Stores whether the person is a student, staff member or visitor. |

**Why these classes are needed:** Each class has one clear responsibility. For example, ParkingSlot manages one slot, while ParkingFacility manages all slots. This makes the program easier to understand and update.

**Business rules**
1. One slot can contain only one vehicle.
2. A ticket number must be unique.
3. A vehicle must have a registration number.
4. An occupied slot cannot be allocated again.
5. A released slot becomes available.
6. Students and staff with a valid monthly pass park free; visitors pay the fee.

**Assumptions:** The program uses a fixed number of floors and slots. Time is entered in minutes for the simple fee example. A started hour is charged as a full hour.

**Alternative: objects vs parallel arrays:** With objects, registration number, type and entry time are stored together in one Vehicle object. Searching is O(n) in a simple list. With parallel arrays, the same vehicle's details are stored at the same index in separate arrays, so all arrays must be updated together. Search is also O(n). Objects use some extra object/reference memory, but adding an attribute is easier because it is added to the class instead of creating and maintaining another parallel array.

## Task 2 – Choose the Data Representation

| Data | Java type | Reason |
|---|---|---|
| Floor number | int | Whole number |
| Slot number | int | Whole number |
| Registration number | String | Contains letters and digits |
| Entry time | long | Can store time in milliseconds |
| Duration | int | Number of minutes |
| Fee | double | Can contain decimal values |

A two-dimensional array works when every floor has the same number of slots. A jagged array (`String[][]`) is better when each floor has a different number of slots. The Task 3 program uses a jagged array.

**Casting and precision:** `130 / 60` using integer division gives `2`, but 130 minutes must be charged as 3 started hours. The program uses `(minutes + 59) / 60` to round up without using decimal division.

**Input:** `Scanner` can read user input from the keyboard. Command-line arguments can also be used for small test programs.

**Alternative representation:** A one-dimensional array needs a formula to convert floor and slot into one index. A single formula is inconvenient when floors have different slot counts. An `ArrayList<String[]>` can store one array per floor, but it is more advanced than a simple jagged array. Locating a slot by scanning is O(F × S) in the worst case. The total storage is O(total slots).

## Task 3 – Design the Control Logic

**Slot allocation pseudocode**
```text
START
FOR each floor
    FOR each slot in that floor
        IF slot is empty
            Store vehicle number
            RETURN floor and slot
        END IF
    END FOR
END FOR
RETURN no slot available
STOP
```

**Fee pseudocode**
```text
START
IF minutes <= 0
    RETURN 0
END IF
hours = (minutes + 59) / 60
fee = hours * hourly rate
RETURN fee
STOP
```

The `break` statement can stop a loop when a slot is found. `continue` can skip an occupied slot. `return` immediately sends the result back to the caller. If no slot is free, allocation returns `null`.

**Completed code:** See `Task03/ParkingFacility.java`.

**Alternative: free-slot stack:** A stack of available slot positions can provide O(1) allocation and release, provided the stack is maintained correctly. The simple scanning solution takes O(F × S) in the worst case for allocation and O(1) for release after validation. A stack needs extra storage for the free positions, up to O(total slots). The scanning version does not need a separate stack.

## Task 4 – Design Classes, Objects and Constructors

A class is a blueprint. An object is an instance created from that class. The Vehicle class can store registration number and type. ParkingTicket can store ticket details.

A default constructor takes no arguments. A parameterised constructor receives values when the object is created. Overloaded constructors have the same name but different parameter lists. The `this` keyword refers to the current object and can distinguish fields from constructor parameters.

A copy constructor is not necessary for this simple system because a new ticket should receive a new ticket number, not copy an old ticket number.

Objects are created in heap memory using `new`. Reference variables hold references to those objects. Local reference variables are commonly stored in a method's stack frame.

**Alternative:** Creating an empty object and then calling setters can temporarily leave an object with missing or invalid values. A parameterised constructor can check required values during creation. Static factory methods are another option, but are not needed for this beginner project. Creating n objects takes O(n) total time and O(n) object storage in all these approaches; the main difference is validation and code organisation.

## Task 5 – Apply static Members and Object Life Cycle

A ticket counter should be `static` because all tickets share one counter. If it were an instance variable, each facility or ticket could have a separate counter and duplicate numbers could occur. A static method can return the next ticket number because it does not need one particular ticket object.

After a vehicle exits, its ticket can be removed from the active-ticket collection. If no references point to that ticket, it becomes eligible for garbage collection. Java decides when memory is reclaimed.

Objects can be passed as method arguments and returned from methods. The reference is passed, so a method can access the same object.

**Alternative:** If the counter belongs to each ParkingFacility, two facilities can each start at ticket number 1. That is acceptable only if ticket numbers are unique within each facility. For campus-wide unique ticket numbers, use one shared static counter. A static counter uses one counter value; an instance counter uses one value per facility object (O(k) for k facilities).

## Task 6 – Design the Inheritance Hierarchy

The superclass is `Vehicle`. Subclasses are `TwoWheeler`, `Car` and `ElectricCar`. This is hierarchical inheritance because multiple subclasses inherit from Vehicle. ElectricCar extends Car, so it also demonstrates multilevel inheritance.

Common information such as registration number is stored in Vehicle. Each subclass supplies its own hourly rate. The `super` keyword calls the parent constructor.

**Constructor trace for ElectricCar:**
1. `new ElectricCar(...)` starts the ElectricCar constructor.
2. `super(...)` calls the Car constructor.
3. Car calls `super(...)` to call the Vehicle constructor.
4. Vehicle stores the registration number.
5. Control returns to Car, then ElectricCar.

**Simple UML**
```text
             Vehicle (abstract)
             /              \
      TwoWheeler             Car
                                |
                           ElectricCar
                                |
                         implements Chargeable
```

**Alternative: composition:** A Vehicle could contain a VehicleType object that stores the rate. Adding a type would mean creating another type object rather than another Vehicle subclass. Both approaches calculate a fee in O(1). Composition can make changing vehicle types easier, while inheritance is simple for this assignment.

## Task 7 – Override and Specialise Behaviour

The `ratePerHour()` method is overridden by each vehicle subclass. The `@Override` annotation helps the compiler check that the method correctly overrides a parent method.

Overriding requires a compatible method signature and return type, and the subclass cannot reduce the access level. The `final` keyword can protect a value that must not change, prevent a method from being overridden, or prevent a class from being extended.

`equals()` compares registration numbers so that two Vehicle objects with the same registration number are treated as equal. `hashCode()` uses the same registration number so it is consistent with `equals()`. `toString()` can be overridden to display useful vehicle details.

**Alternative: switch statement:** One method could check a type field using `switch`. Fee calculation would still be O(1) for a fixed number of types. However, adding a new type requires editing the switch method. With overriding, a new subclass can provide its own rate. The switch approach has fewer classes but can become harder to maintain.

## Task 8 – Apply Polymorphism

Compile-time polymorphism is method overloading. For example, methods can be named `calculateFee(int minutes)` and `calculateFee(long minutes)`. The compiler chooses based on the argument type.

Runtime polymorphism occurs when a Vehicle reference points to a subclass object. A `Vehicle[]` can store TwoWheeler, Car and ElectricCar objects. When `calculateFee()` is called, Java runs the overridden method for the actual object.

**Dispatch trace:** Declared type = `Vehicle`; actual object = `Car`; method called = `Car.ratePerHour()` through the Vehicle fee calculation.

This avoids writing a long if-else chain for every vehicle type.

**Completed code:** See `Task08/FeeDemo.java`.

**Alternative: instanceof checks:** The program could check each object type and cast it before calculating a fee. For k vehicle types, it may perform up to k checks. Dynamic dispatch selects the correct overridden method directly. Both fee calculations are O(1) for a fixed set of classes, but dynamic dispatch is easier to extend.

## Task 9 – Introduce Abstraction

`Vehicle` is abstract because a general vehicle does not have one fixed hourly rate. Its `ratePerHour()` method is abstract and is implemented by subclasses.

`Chargeable` is an interface because charging is a capability that only some vehicles have. ElectricCar implements it. A class can implement more than one interface, so it can have multiple capabilities.

An abstract class can store common fields and constructors. An interface describes behaviour that implementing classes promise to provide. A default interface method can provide a common method body; a static interface method can be called using the interface name.

**Alternative:** If Vehicle were an interface, it could not use a normal instance constructor to initialise registration number. State would need to be stored in implementing classes. If Chargeable became an abstract class, a class already extending Vehicle could not also extend it because Java classes have single inheritance. Method-call time is effectively O(1) in both simple designs; object storage depends on the fields used.

## Task 10 – Enforce Encapsulation through Packages and Access Modifiers

Suggested package structure:
```text
parking
  model
  service
  app
```

Fields should normally be `private` so other classes cannot change them directly. Public getters provide controlled access. Setters should validate values, for example rejecting a negative parking duration or invalid slot number. `protected` is useful for members intended for subclasses. Default access is limited to the same package.

Wrapper classes such as `Integer` can be used when an object is required. Autoboxing converts between `int` and `Integer`. Comparing two Integer objects using `==` can compare references instead of numeric values; use `.equals()` to compare their values.

**Alternative: public fields:** Any class could change a field to an invalid value, such as a negative fee. Validation would need to be repeated in every calling class. A setter performs a constant-time O(1) validation per update and keeps the check in one place.

## Task 11 – Implement, Test and Defend the Design

**Working implementation:** See `Task11/SmartParkingSystem.java`.

The program demonstrates vehicle types, slot allocation, ticket creation, fee calculation and slot release. It uses simple arrays and classes.

**Test plan**

| Test | Expected result |
|---|---|
| Park a two-wheeler in an empty facility | A free slot is allocated |
| Park a car after the first vehicle | A different free slot is allocated |
| Fill all slots and try another vehicle | No slot available |
| Release a valid occupied slot | Slot is released |
| Release the same slot again | Release fails |
| Calculate a 61-minute stay | Two started hours are charged |
| Student/staff with valid monthly pass | Parking fee is zero |

Run the program and compare its actual output with these expected results. Add your own test cases and screenshots.

**Daily collection example:** One two-wheeler parked for 61 minutes at Rs 10 per hour pays Rs 20. One car parked for 60 minutes at Rs 30 per hour pays Rs 30. Total collection is Rs 50. A valid monthly-pass holder pays Rs 0.

**Evaluation:** Encapsulation protects the slot and vehicle data. Inheritance shares common vehicle details. Polymorphism allows each vehicle to have its own rate. Abstraction defines common vehicle behaviour. One limitation is that this simple program does not save data permanently after it closes.

**Alternative data structures:** An array requires a search through up to n tickets, O(n). An ArrayList also requires O(n) search by registration number. A HashMap keyed by registration number usually gives average O(1) lookup and uses O(n) space, but it is a little more advanced.

## Task 12 – Re-evaluate and Adapt the Design

**A. Impact analysis:** The new requirements add e-bikes, delivery vans and one-day visitor passes. The number of floors and the number of slots on each floor can differ. A jagged array is suitable because each floor can have its own slot count.

**B. Evaluation:** The original Vehicle superclass and subclasses are still useful. New vehicle types can be added as subclasses. The delivery-van time-based pricing rule needs additional fee logic. The one-day pass needs a separate pass field or class. Therefore, the original design can be modified rather than completely redesigned.

**C. Adaptation:** The sample in `Task12/DesignChallenge.java` adds E-bike and DeliveryVan classes, uses a jagged array for slots, and gives visitors a one-day pass option. The example keeps the code simple and places the special pricing rule in the relevant vehicle method.

**D. Evidence-based defence:** The design is modified by adding new classes and a pass option. Existing common vehicle and slot concepts can remain. The test output demonstrates the new vehicle categories and pass rule. A future vehicle type can be added as another subclass, although a more flexible pricing-rule design may be useful if many special rules are introduced.

---

## Final submission checklist
- [ ] Replace student details.
- [ ] Run all Java files.
- [ ] Capture output screenshots from your own computer.
- [ ] Add the GitHub repository link.
- [ ] Commit task folders progressively.
- [ ] Check that the repository is accessible to your faculty.
