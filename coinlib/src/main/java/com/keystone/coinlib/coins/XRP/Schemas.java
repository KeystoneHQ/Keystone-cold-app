/*
 *
 *  Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.keystone.coinlib.coins.XRP;

public interface Schemas {
    String AccountSet = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"ClearFlag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Domain\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"EmailHash\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"MessageKey\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"SetFlag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"TransferRate\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"TickSize\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\",\n" +
            "    \"Sequence\"\n" +
            "  ]\n" +
            "}";

    String AccountDelete = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Destination\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"DestinationTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\",\n" +
            "    \"Sequence\",\n" +
            "    \"Destination\"\n" +
            "  ]\n" +
            "}";

    String CheckCancel = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"CheckID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\",\n" +
            "    \"CheckID\"\n" +
            "  ]\n" +
            "}";

    String CheckCash = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"CheckID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\",\n" +
            "    \"CheckID\"\n" +
            "  ]\n" +
            "}";

    String CheckCreate = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Destination\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"DestinationTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Expiration\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"InvoiceID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\",\n" +
            "    \"Destination\",\n" +
            "    \"SendMax\"\n" +
            "  ]\n" +
            "}";

    String DepositPreauth = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Authorize\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Unauthorize\": {\n" +
            "      \"type\": \"string\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\",\n" +
            "    \"Sequence\"\n" +
            "  ]\n" +
            "}";

    String EscrowCancel = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Owner\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"OfferSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Owner\",\n" +
            "    \"OfferSequence\"\n" +
            "  ]\n" +
            "}\n";

    String EscrowCreate = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Amount\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Destination\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"CancelAfter\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"FinishAfter\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Condition\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"DestinationTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Amount\",\n" +
            "    \"Destination\"\n" +
            "  ]\n" +
            "}";

    String EscrowFinish = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Owner\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"OfferSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Condition\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fulfillment\": {\n" +
            "      \"type\": \"string\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Owner\",\n" +
            "    \"OfferSequence\"\n" +
            "  ]\n" +
            "}\n";

    String OfferCancel = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"OfferSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\",\n" +
            "    \"Sequence\",\n" +
            "    \"OfferSequence\"\n" +
            "  ]\n" +
            "}\n";

    String OfferCreate = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Expiration\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"OfferSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\",\n" +
            "    \"Sequence\",\n" +
            "    \"TakerGets\",\n" +
            "    \"TakerPays\"\n" +
            "  ]\n" +
            "}";

    String Payment = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },    \n" +
            "    \"Destination\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"DestinationTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"InvoiceID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\",\n" +
            "    \"Sequence\",\n" +
            "    \"Amount\",\n" +
            "    \"Destination\"\n" +
            "  ]\n" +
            "}";

    String PaymentChannelClaim = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Channel\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Balance\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Amount\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Signature\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"PublicKey\": {\n" +
            "      \"type\": \"string\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"TransactionType\",\n" +
            "    \"Channel\"\n" +
            "  ]\n" +
            "}";

    String PaymentChannelCreate = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Amount\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Destination\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"SettleDelay\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"PublicKey\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },     \n" +
            "    \"CancelAfter\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"DestinationTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Amount\",\n" +
            "    \"Destination\",\n" +
            "    \"SettleDelay\",\n" +
            "    \"PublicKey\"\n" +
            "  ]\n" +
            "}";

    String PaymentChannelFund = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"Channel\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Amount\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Expiration\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Channel\",\n" +
            "    \"Amount\"\n" +
            "  ]\n" +
            "}";

    String SetRegularKey = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"RegularKey\": {\n" +
            "      \"type\": \"string\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\"\n" +
            "  ]\n" +
            "}";

    String SignerListSet = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SignerQuorum\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SignerEntries\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"items\": {\n" +
            "        \"type\": \"object\",\n" +
            "        \"properties\": {\n" +
            "          \"SignerEntry\": {\n" +
            "            \"type\": \"object\",\n" +
            "            \"properties\": {\n" +
            "              \"Account\": {\n" +
            "                \"type\": \"string\"\n" +
            "              },\n" +
            "              \"SignerWeight\": {\n" +
            "                \"type\": \"integer\"\n" +
            "              }\n" +
            "            },\n" +
            "            \"required\": [\n" +
            "              \"Account\",\n" +
            "              \"SignerWeight\"\n" +
            "            ]\n" +
            "          }\n" +
            "        },\n" +
            "        \"required\": [\n" +
            "          \"SignerEntry\"\n" +
            "        ]\n" +
            "      },\n" +
            "    \"minItems\": 1,\n" +
            "    \"maxItems\": 8\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\",\n" +
            "    \"SignerQuorum\"\n" +
            "  ]\n" +
            "}\n";

    String TrustSet = "{\n" +
            "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
            "  \"type\": \"object\",\n" +
            "  \"properties\": {\n" +
            "    \"Account\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"TransactionType\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Fee\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Sequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"AccountTxnID\": {\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    \"Flags\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LastLedgerSequence\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"SourceTag\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"LimitAmount\": {\n" +
            "      \"type\": \"object\",\n" +
            "      \"properties\": {\n" +
            "        \"currency\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"issuer\": {\n" +
            "          \"type\": \"string\"\n" +
            "        },\n" +
            "        \"value\": {\n" +
            "          \"type\": \"string\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"required\": [\n" +
            "        \"currency\",\n" +
            "        \"issuer\",\n" +
            "        \"value\"\n" +
            "      ]\n" +
            "    },\n" +
            "    \"QualityIn\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    },\n" +
            "    \"QualityOut\": {\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"required\": [\n" +
            "    \"Account\",\n" +
            "    \"TransactionType\",\n" +
            "    \"Fee\",\n" +
            "    \"Sequence\",\n" +
            "    \"LimitAmount\"\n" +
            "  ]\n" +
            "}";

}
