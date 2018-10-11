package net.consensys.pantheon.ethereum.mainnet;

import static net.consensys.pantheon.ethereum.vm.OperationTracer.NO_TRACING;

import net.consensys.pantheon.ethereum.chain.Blockchain;
import net.consensys.pantheon.ethereum.core.Address;
import net.consensys.pantheon.ethereum.core.LogSeries;
import net.consensys.pantheon.ethereum.core.ProcessableBlockHeader;
import net.consensys.pantheon.ethereum.core.Transaction;
import net.consensys.pantheon.ethereum.core.WorldUpdater;
import net.consensys.pantheon.ethereum.mainnet.TransactionValidator.TransactionInvalidReason;
import net.consensys.pantheon.ethereum.vm.OperationTracer;
import net.consensys.pantheon.util.bytes.BytesValue;

/** Processes transactions. */
public interface TransactionProcessor {

  /** A transaction processing result. */
  interface Result {

    /** The status of the transaction after being processed. */
    enum Status {

      /** The transaction was invalid for processing. */
      INVALID,

      /** The transaction was successfully processed. */
      SUCCESSFUL,

      /** The transaction failed to be completely processed. */
      FAILED
    }

    /**
     * Return the logs produced by the transaction.
     *
     * <p>This is only valid when {@code TransactionProcessor#isSuccessful} returns {@code true}.
     *
     * @return the logs produced by the transaction
     */
    LogSeries getLogs();

    /**
     * Returns the status of the transaction after being processed.
     *
     * @return the status of the transaction after being processed
     */
    Status getStatus();

    /**
     * Returns the gas remaining after the transaction was processed.
     *
     * <p>This is only valid when {@code TransactionProcessor#isSuccessful} returns {@code true}.
     *
     * @return the gas remaining after the transaction was processed
     */
    long getGasRemaining();

    BytesValue getOutput();

    /**
     * Returns whether or not the transaction was invalid.
     *
     * @return {@code true} if the transaction was invalid; otherwise {@code false}
     */
    default boolean isInvalid() {
      return getStatus() == Status.INVALID;
    }

    /**
     * Returns whether or not the transaction was successfully processed.
     *
     * @return {@code true} if the transaction was successfully processed; otherwise {@code false}
     */
    default boolean isSuccessful() {
      return getStatus() == Status.SUCCESSFUL;
    }

    /**
     * Returns the transaction validation result.
     *
     * @return the validation result, with the reason for failure (if applicable.)
     */
    ValidationResult<TransactionInvalidReason> getValidationResult();
  }

  /**
   * Applies a transaction to the current system state.
   *
   * @param blockchain The current blockchain
   * @param worldState The current world state
   * @param blockHeader The current block header
   * @param transaction The transaction to process
   * @param miningBeneficiary the address which is to receive the transaction fee
   * @return the transaction result
   */
  default Result processTransaction(
      final Blockchain blockchain,
      final WorldUpdater worldState,
      final ProcessableBlockHeader blockHeader,
      final Transaction transaction,
      final Address miningBeneficiary) {
    return processTransaction(
        blockchain, worldState, blockHeader, transaction, miningBeneficiary, NO_TRACING);
  }

  /**
   * Applies a transaction to the current system state.
   *
   * @param blockchain The current blockchain
   * @param worldState The current world state
   * @param blockHeader The current block header
   * @param transaction The transaction to process
   * @param operationTracer The tracer to record results of each EVM operation
   * @param miningBeneficiary the address which is to receive the transaction fee
   * @return the transaction result
   */
  Result processTransaction(
      Blockchain blockchain,
      WorldUpdater worldState,
      ProcessableBlockHeader blockHeader,
      Transaction transaction,
      Address miningBeneficiary,
      OperationTracer operationTracer);
}