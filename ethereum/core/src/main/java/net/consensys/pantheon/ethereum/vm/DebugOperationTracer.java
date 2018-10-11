package net.consensys.pantheon.ethereum.vm;

import static net.consensys.pantheon.util.uint.UInt256.U_32;

import net.consensys.pantheon.ethereum.core.Gas;
import net.consensys.pantheon.ethereum.debug.TraceFrame;
import net.consensys.pantheon.ethereum.debug.TraceOptions;
import net.consensys.pantheon.ethereum.vm.ehalt.ExceptionalHaltException;
import net.consensys.pantheon.util.bytes.Bytes32;
import net.consensys.pantheon.util.uint.UInt256;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class DebugOperationTracer implements OperationTracer {

  private final TraceOptions options;
  private final List<TraceFrame> traceFrames = new ArrayList<>();

  public DebugOperationTracer(final TraceOptions options) {
    this.options = options;
  }

  @Override
  public void traceExecution(
      final MessageFrame frame,
      final Optional<Gas> currentGasCost,
      final ExecuteOperation executeOperation)
      throws ExceptionalHaltException {
    final int depth = frame.getMessageStackDepth();
    final String opcode = frame.getCurrentOperation().getName();
    final int pc = frame.getPC();
    final Gas gasRemaining = frame.getRemainingGas();
    final EnumSet<ExceptionalHaltReason> exceptionalHaltReasons =
        EnumSet.copyOf(frame.getExceptionalHaltReasons());
    final Optional<Bytes32[]> stack = captureStack(frame);
    final Optional<Bytes32[]> memory = captureMemory(frame);

    try {
      executeOperation.execute();
    } finally {
      final Optional<Map<UInt256, UInt256>> storage = captureStorage(frame);

      traceFrames.add(
          new TraceFrame(
              pc,
              opcode,
              gasRemaining,
              currentGasCost,
              depth,
              exceptionalHaltReasons,
              stack,
              memory,
              storage));
    }
  }

  private Optional<Map<UInt256, UInt256>> captureStorage(final MessageFrame frame) {
    if (!options.isStorageEnabled()) {
      return Optional.empty();
    }
    final Map<UInt256, UInt256> storageContents =
        new TreeMap<>(
            frame.getWorldState().getMutable(frame.getRecipientAddress()).getUpdatedStorage());
    return Optional.of(storageContents);
  }

  private Optional<Bytes32[]> captureMemory(final MessageFrame frame) {
    if (!options.isMemoryEnabled()) {
      return Optional.empty();
    }
    final Bytes32[] memoryContents = new Bytes32[frame.memoryWordSize().toInt()];
    for (int i = 0; i < memoryContents.length; i++) {
      memoryContents[i] = Bytes32.wrap(frame.readMemory(UInt256.of(i).times(U_32), U_32), 0);
    }
    return Optional.of(memoryContents);
  }

  private Optional<Bytes32[]> captureStack(final MessageFrame frame) {
    if (!options.isStackEnabled()) {
      return Optional.empty();
    }
    final Bytes32[] stackContents = new Bytes32[frame.stackSize()];
    for (int i = 0; i < stackContents.length; i++) {
      // Record stack contents in reverse
      stackContents[i] = frame.getStackItem(stackContents.length - i - 1);
    }
    return Optional.of(stackContents);
  }

  public List<TraceFrame> getTraceFrames() {
    return traceFrames;
  }
}