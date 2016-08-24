/*
 * Copyright 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * The original file is available at:
 * https://gwt.googlesource.com/gwt-benchmarks/+/master/benchmarks/src/main/
 *         java/com/google/gwt/benchmark/benchmarks/octane/client/richards/gwt/
 *
 * Modifications:
 * (1) Squashed java files into a single java file.
 * (1) Added timeRichards() method for running with different iterations.
 * (2) Added verifyRichards() method for verifying the benchmark.
 * (3) Changed main() method.
 * (4) Changed arrays from using GWT library classes (JavaScriptArray,
 *     CollectionFactory, etc) into standard java arrays.
 */

package benchmarks.algorithm;

// CHECKSTYLE.OFF: .*
class DeviceTask implements Task {

  private Scheduler scheduler;
  private Packet v1;
  /**
   * A task that suspends itself after each time it has been run to simulate
   * waiting for data from an external device.
   * @param {Scheduler} scheduler the scheduler that manages this task
   * @constructor
   */
  @SuppressWarnings("javadoc")
  public DeviceTask(Scheduler scheduler) {
    this.scheduler = scheduler;
    this.v1 = null;
  }

  @Override
  public TaskControlBlock run(Packet packet) {
    if (packet == null) {
      if (this.v1 == null) return this.scheduler.suspendCurrent();
      Packet v = this.v1;
      this.v1 = null;
      return this.scheduler.queue(v);
    } else {
      this.v1 = packet;
      return this.scheduler.holdCurrent();
    }
  }

  @Override
  public String toString() {
    return "DeviceTask";
  }
}

class HandlerTask implements Task{

  private Scheduler scheduler;
  private Packet v1;
  private Packet v2;

  /**
   * A task that manipulates work packets and then suspends itself.
   * @param {Scheduler} scheduler the scheduler that manages this task
   * @constructor
   */
  @SuppressWarnings("javadoc")
  public HandlerTask(Scheduler scheduler) {
    this.scheduler = scheduler;
    this.v1 = null;
    this.v2 = null;
  }

  @Override
  public TaskControlBlock run(Packet packet) {
    if (packet != null) {
      if (packet.kind == Scheduler.KIND_WORK) {
        this.v1 = packet.addTo(this.v1);
      } else {
        this.v2 = packet.addTo(this.v2);
      }
    }
    if (this.v1 != null) {
      int count = this.v1.a1;
      Packet v;
      if (count < Packet.DATA_SIZE) {
        if (this.v2 != null) {
          v = this.v2;
          this.v2 = this.v2.link;
          v.a1 = this.v1.a2[count];
          this.v1.a1 = count + 1;
          return this.scheduler.queue(v);
        }
      } else {
        v = this.v1;
        this.v1 = this.v1.link;
        return this.scheduler.queue(v);
      }
    }
    return this.scheduler.suspendCurrent();
  }

  @Override
  public String toString() {
    return "HandlerTask";
  }
}

class IdleTask implements Task {

  private Scheduler scheduler;
  private int v1;
  private int count;
  /**
   * An idle task doesn't do any work itself but cycles control between the two
   * device tasks.
   * @param {Scheduler} scheduler the scheduler that manages this task
   * @param {int} v1 a seed value that controls how the device tasks are scheduled
   * @param {int} count the number of times this task should be scheduled
   * @constructor
   */
  @SuppressWarnings("javadoc")
  public IdleTask(Scheduler scheduler, int v1, int count) {
    this.scheduler = scheduler;
    this.v1 = v1;
    this.count = count;
  }

  @Override
  public TaskControlBlock run(Packet packet) {
    this.count--;
    if (this.count == 0) return this.scheduler.holdCurrent();
    if ((this.v1 & 1) == 0) {
      this.v1 = this.v1 >> 1;
      return this.scheduler.release(Scheduler.ID_DEVICE_A);
    } else {
      this.v1 = (this.v1 >> 1) ^ 0xD008;
      return this.scheduler.release(Scheduler.ID_DEVICE_B);
    }
  }

  @Override
  public String toString() {
    return "IdleTask";
  }
}

class Packet {

  public static final int DATA_SIZE = 4;

  public Packet link;

  public int id;

  public int a1;

  public int[] a2;

  public int kind;

  /**
   * A simple package of data that is manipulated by the tasks. The exact layout of the payload data
   * carried by a packet is not importaint, and neither is the nature of the work performed on
   * packets by the tasks.
   *
   * Besides carrying data, packets form linked lists and are hence used both as data and
   * worklists.
   *
   * @param {Packet} link the tail of the linked list of packets
   * @param {int} id an ID for this packet
   * @param {int} kind the type of this packet
   * @constructor
   */
  @SuppressWarnings("javadoc")
  public Packet(Packet link, int id, int kind) {
    this.link = link;
    this.id = id;
    this.kind = kind;
    this.a1 = 0;
    this.a2 = new int[DATA_SIZE];
  }

  /**
   * Add this packet to the end of a worklist, and return the worklist.
   *
   * @param {Packet} queue the worklist to add this packet to
   */
  @SuppressWarnings("javadoc")
  public Packet addTo(Packet queue) {
    this.link = null;
    if (queue == null)
      return this;
    Packet peek;
    Packet next = queue;
    while ((peek = next.link) != null)
      next = peek;
    next.link = this;
    return queue;
  }

  @Override
  public String toString() {
    return "Packet";
  }

}

/**
 * A scheduler can be used to schedule a set of tasks based on their relative priorities. Scheduling
 * is done by maintaining a list of task control blocks which holds tasks and the data queue they
 * are processing.
 *
 * @constructor
 */
class Scheduler {

  public static final int ID_IDLE = 0;
  public static final int ID_WORKER = 1;
  public static final int ID_HANDLER_A = 2;
  public static final int ID_HANDLER_B = 3;
  public static final int ID_DEVICE_A = 4;
  public static final int ID_DEVICE_B = 5;
  public static final int NUMBER_OF_IDS = 6;

  public static final int KIND_DEVICE = 0;
  public static final int KIND_WORK = 1;

  public int queueCount;
  public int holdCount;
  private TaskControlBlock list;
  private TaskControlBlock currentTcb;
  private int currentId;
  private TaskControlBlock[] blocks;

  public Scheduler() {
    this.queueCount = 0;
    this.holdCount = 0;
    this.blocks = new TaskControlBlock[NUMBER_OF_IDS];
    this.list = null;
    this.currentTcb = null;
    this.currentId = -1;
  }

  /**
   * Add an idle task to this scheduler.
   * @param {int} id the identity of the task
   * @param {int} priority the task's priority
   * @param {Packet} queue the queue of work to be processed by the task
   * @param {int} count the number of times to schedule the task
   */
  @SuppressWarnings("javadoc")
  public void addIdleTask(int id, int priority, Packet queue, int count) {
    this.addRunningTask(id, priority, queue, new IdleTask(this, 1, count));
  }

  /**
   * Add a work task to this scheduler.
   * @param {int} id the identity of the task
   * @param {int} priority the task's priority
   * @param {Packet} queue the queue of work to be processed by the task
   */
  @SuppressWarnings("javadoc")
  public void addWorkerTask(int id, int priority, Packet queue) {
    this.addTask(id, priority, queue, new WorkerTask(this, ID_HANDLER_A, 0));
  }

  /**
   * Add a handler task to this scheduler.
   * @param {int} id the identity of the task
   * @param {int} priority the task's priority
   * @param {Packet} queue the queue of work to be processed by the task
   */
  @SuppressWarnings("javadoc")
  public void addHandlerTask(int id, int priority, Packet queue) {
    this.addTask(id, priority, queue, new HandlerTask(this));
  }

  /**
   * Add a handler task to this scheduler.
   * @param {int} id the identity of the task
   * @param {int} priority the task's priority
   * @param {Packet} queue the queue of work to be processed by the task
   */
  @SuppressWarnings("javadoc")
  public void addDeviceTask(int id, int priority, Packet queue) {
    this.addTask(id, priority, queue, new DeviceTask(this));
  }

  /**
   * Add the specified task and mark it as running.
   * @param {int} id the identity of the task
   * @param {int} priority the task's priority
   * @param {Packet} queue the queue of work to be processed by the task
   * @param {Task} task the task to add
   */
  @SuppressWarnings("javadoc")
  public void addRunningTask(int id, int priority, Packet queue, Task task) {
    this.addTask(id, priority, queue, task);
    this.currentTcb.setRunning();
  }

  /**
   * Add the specified task to this scheduler.
   * @param {int} id the identity of the task
   * @param {int} priority the task's priority
   * @param {Packet} queue the queue of work to be processed by the task
   * @param {Task} task the task to add
   */
  @SuppressWarnings("javadoc")
  private void addTask(int id, int priority, Packet queue, Task task) {
    this.currentTcb = new TaskControlBlock(this.list, id, priority, queue, task);
    this.list = this.currentTcb;
    this.blocks[id] = this.currentTcb;
  }

  /**
   * Execute the tasks managed by this scheduler.
   */
  public void schedule() {
    this.currentTcb = this.list;
    while (this.currentTcb != null) {
      if (this.currentTcb.isHeldOrSuspended()) {
        this.currentTcb = this.currentTcb.link;
      } else {
        this.currentId = this.currentTcb.id;
        this.currentTcb = this.currentTcb.run();
      }
    }
  }

  /**
   * Release a task that is currently blocked and return the next block to run.
   * @param {int} id the id of the task to suspend
   */
  @SuppressWarnings("javadoc")
  public TaskControlBlock release(int id) {
    TaskControlBlock tcb = this.blocks[id];
    if (tcb == null) return tcb;
    tcb.markAsNotHeld();
    if (tcb.priority > this.currentTcb.priority) {
      return tcb;
    } else {
      return this.currentTcb;
    }
  }

  /**
   * Block the currently executing task and return the next task control block
   * to run.  The blocked task will not be made runnable until it is explicitly
   * released, even if new work is added to it.
   */
  public TaskControlBlock holdCurrent() {
    this.holdCount++;
    this.currentTcb.markAsHeld();
    return this.currentTcb.link;
  }

  /**
   * Suspend the currently executing task and return the next task control block
   * to run.  If new work is added to the suspended task it will be made runnable.
   */
  public TaskControlBlock suspendCurrent() {
    this.currentTcb.markAsSuspended();
    return this.currentTcb;
  }

  /**
   * Add the specified packet to the end of the worklist used by the task
   * associated with the packet and make the task runnable if it is currently
   * suspended.
   * @param {Packet} packet the packet to add
   */
  @SuppressWarnings("javadoc")
  public TaskControlBlock queue(Packet packet) {
    TaskControlBlock t = this.blocks[packet.id];
    if (t == null) return t;
    this.queueCount++;
    packet.link = null;
    packet.id = this.currentId;
    return t.checkPriorityAdd(this.currentTcb, packet);
  }

}

class TaskControlBlock implements Task {

  public TaskControlBlock link;

  public int id;

  public int priority;

  private Packet queue;

  private Task task;

  private int state;

  /**
   * A task control block manages a task and the queue of work packages associated
   * with it.
   * @param {TaskControlBlock} link the preceding block in the linked block list
   * @param {int} id the id of this block
   * @param {int} priority the priority of this block
   * @param {Packet} queue the queue of packages to be processed by the task
   * @param {Task} task the task
   * @constructor
   */
  @SuppressWarnings("javadoc")
  public TaskControlBlock(TaskControlBlock link, int id, int priority, Packet queue, Task task) {
    this.link = link;
    this.id = id;
    this.priority = priority;
    this.queue = queue;
    this.task = task;
    if (queue == null) {
      this.state = STATE_SUSPENDED;
    } else {
      this.state = STATE_SUSPENDED_RUNNABLE;
    }
  }

  /**
   * The task is running and is currently scheduled.
   */
  public static final int  STATE_RUNNING = 0;

  /**
   * The task has packets left to process.
   */
  public static final int STATE_RUNNABLE = 1;

  /**
   * The task is not currently running.  The task is not blocked as such and may
  * be started by the scheduler.
   */
  public static final int STATE_SUSPENDED = 2;

  /**
   * The task is blocked and cannot be run until it is explicitly released.
   */
  public static final int STATE_HELD = 4;

  public static final int STATE_SUSPENDED_RUNNABLE = STATE_SUSPENDED | STATE_RUNNABLE;
  public static final int STATE_NOT_HELD = ~STATE_HELD;

  public void setRunning() {
    this.state = STATE_RUNNING;
  }

  public void markAsNotHeld () {
    this.state = this.state & STATE_NOT_HELD;
  }

  public void markAsHeld() {
    this.state = this.state | STATE_HELD;
  }

  public boolean isHeldOrSuspended() {
    return (this.state & STATE_HELD) != 0 || (this.state == STATE_SUSPENDED);
  }

  public void markAsSuspended() {
    this.state = this.state | STATE_SUSPENDED;
  }

  public void markAsRunnable() {
    this.state = this.state | STATE_RUNNABLE;
  }

  /**
   * Runs this task, if it is ready to be run, and returns the next task to run.
   */
  public TaskControlBlock run() {
    Packet packet;
    if (this.state == STATE_SUSPENDED_RUNNABLE) {
      packet = this.queue;
      this.queue = packet.link;
      if (this.queue == null) {
        this.state = STATE_RUNNING;
      } else {
        this.state = STATE_RUNNABLE;
      }
    } else {
      packet = null;
    }
    return this.task.run(packet);
  }

  /**
   * Adds a packet to the worklist of this block's task, marks this as runnable if
   * necessary, and returns the next runnable object to run (the one
   * with the highest priority).
   */
  public TaskControlBlock checkPriorityAdd(TaskControlBlock task, Packet packet) {
    if (this.queue == null) {
      this.queue = packet;
      this.markAsRunnable();
      if (this.priority > task.priority) return this;
    } else {
      this.queue = packet.addTo(this.queue);
    }
    return task;
  }

  @Override
  public String toString() {
    return "tcb { " + this.task + "@" + this.state + " }";
  }

  @Override
  public TaskControlBlock run(Packet packet) {
    return run();
  }
}

interface Task {
  TaskControlBlock run(Packet packet);
}

class WorkerTask implements Task {

  private Scheduler scheduler;
  private int v1;
  private int v2;
  /**
   * A task that manipulates work packets.
   * @param {Scheduler} scheduler the scheduler that manages this task
   * @param {int} v1 a seed used to specify how work packets are manipulated
   * @param {int} v2 another seed used to specify how work packets are manipulated
   * @constructor
   */
  @SuppressWarnings("javadoc")
  public WorkerTask(Scheduler scheduler, int v1, int v2) {
    this.scheduler = scheduler;
    this.v1 = v1;
    this.v2 = v2;
  }

  @Override
  public TaskControlBlock run(Packet packet) {
    if (packet == null) {
      return this.scheduler.suspendCurrent();
    } else {
      if (this.v1 == Scheduler.ID_HANDLER_A) {
        this.v1 = Scheduler.ID_HANDLER_B;
      } else {
        this.v1 = Scheduler.ID_HANDLER_A;
      }
      packet.id = this.v1;
      packet.a1 = 0;
      for (int i = 0; i < Packet.DATA_SIZE; i++) {
        this.v2++;
        if (this.v2 > 26) this.v2 = 1;
        packet.a2[i] = this.v2;
      }
      return this.scheduler.queue(packet);
    }
  }

  @Override
  public String toString() {
    return "WorkerTask";
  }
}

/**
 * This is the Java port of the JavaScript implementation of Richards benchmark from
 * http://www.cl.cam.ac.uk/~mr10/Bench.html
 *
 * This benchmark (written in JavaScript) is part of the V8 benchmark suite.
 */
public class Richards {
  /**
   * The Richards benchmark simulates the task dispatcher of an
   * operating system.
   **/
  public boolean runRichards() {
    Scheduler scheduler = new Scheduler();
    scheduler.addIdleTask(Scheduler.ID_IDLE, 0, null, COUNT);

    Packet queue = new Packet(null, Scheduler.ID_WORKER, Scheduler.KIND_WORK);
    queue = new Packet(queue,  Scheduler.ID_WORKER, Scheduler.KIND_WORK);
    scheduler.addWorkerTask(Scheduler.ID_WORKER, 1000, queue);

    queue = new Packet(null, Scheduler.ID_DEVICE_A, Scheduler.KIND_DEVICE);
    queue = new Packet(queue,  Scheduler.ID_DEVICE_A, Scheduler.KIND_DEVICE);
    queue = new Packet(queue,  Scheduler.ID_DEVICE_A, Scheduler.KIND_DEVICE);
    scheduler.addHandlerTask(Scheduler.ID_HANDLER_A, 2000, queue);

    queue = new Packet(null, Scheduler.ID_DEVICE_B, Scheduler.KIND_DEVICE);
    queue = new Packet(queue,  Scheduler.ID_DEVICE_B, Scheduler.KIND_DEVICE);
    queue = new Packet(queue,  Scheduler.ID_DEVICE_B, Scheduler.KIND_DEVICE);
    scheduler.addHandlerTask(Scheduler.ID_HANDLER_B, 3000, queue);

    scheduler.addDeviceTask(Scheduler.ID_DEVICE_A, 4000, null);

    scheduler.addDeviceTask(Scheduler.ID_DEVICE_B, 5000, null);

    scheduler.schedule();

    if (scheduler.queueCount != EXPECTED_QUEUE_COUNT ||
        scheduler.holdCount != EXPECTED_HOLD_COUNT) {
      String msg =
          "Error during execution: queueCount = " + scheduler.queueCount +
          ", holdCount = " + scheduler.holdCount + ".";
      System.out.println(msg);
      return false;
    }
    return true;
  }

  public static final int COUNT = 1000;
  /**
   * These two constants specify how many times a packet is queued and
   * how many times a task is put on hold in a correct run of richards.
   * They don't have any meaning a such but are characteristic of a
   * correct run so if the actual queue or hold count is different from
   * the expected there must be a bug in the implementation.
   **/
  public static final int EXPECTED_QUEUE_COUNT = 2322;
  public static final int EXPECTED_HOLD_COUNT = 928;

  // CHECKSTYLE.ON: .*
  public void timeRichards(int iterations) {
    for (int iter = 0; iter < iterations; iter++) {
      runRichards();
    }
  }

  public boolean verifyRichards() {
    if (!runRichards()) {
      System.out.println("Run Richards failed");
      return false;
    }
    return true;
  }

  public static void main(String[] args) {
    Richards obj = new Richards();
    long before = System.currentTimeMillis();
    obj.timeRichards(1200);
    long after = System.currentTimeMillis();
    System.out.println("benchmarks/algorithm/Richards: " + (after - before));
  }
}
