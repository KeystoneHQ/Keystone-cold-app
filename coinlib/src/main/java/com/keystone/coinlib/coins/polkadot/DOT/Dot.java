/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.coinlib.coins.polkadot.DOT;

import com.keystone.coinlib.coins.AbsCoin;
import com.keystone.coinlib.coins.AbsDeriver;
import com.keystone.coinlib.coins.AbsTx;
import com.keystone.coinlib.coins.polkadot.AddressCodec;
import com.keystone.coinlib.coins.polkadot.UOS.Network;
import com.keystone.coinlib.coins.polkadot.pallets.Pallet;
import com.keystone.coinlib.coins.polkadot.pallets.Parameter;
import com.keystone.coinlib.coins.polkadot.pallets.balance.Transfer;
import com.keystone.coinlib.coins.polkadot.pallets.balance.TransferKeepAlive;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.Blacklist;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.CancelProposal;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.CancelQueued;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.CancelReferendum;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.ClearPublicProposals;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.Delegate;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.EmergencyCancel;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.EnactProposal;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.ExternalPropose;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.ExternalProposeDefault;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.ExternalProposeMajority;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.FastTrack;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.NoteImminentPreimage;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.NoteImminentPreimageOperational;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.NotePreimage;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.NotePreimageOperational;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.Propose;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.ReapPreimage;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.RemoveOtherVote;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.RemoveVote;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.Second;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.UnDelegate;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.Unlock;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.VetoExternal;
import com.keystone.coinlib.coins.polkadot.pallets.democracy.Vote;
import com.keystone.coinlib.coins.polkadot.pallets.elections_phragmen.RemoveMember;
import com.keystone.coinlib.coins.polkadot.pallets.elections_phragmen.RemoveVoter;
import com.keystone.coinlib.coins.polkadot.pallets.elections_phragmen.RenounceCandidacy;
import com.keystone.coinlib.coins.polkadot.pallets.elections_phragmen.ReportDefunctVoter;
import com.keystone.coinlib.coins.polkadot.pallets.elections_phragmen.SubmitCandidacy;
import com.keystone.coinlib.coins.polkadot.pallets.identity.SetIdentity;
import com.keystone.coinlib.coins.polkadot.pallets.multisig.ApproveAsMulti;
import com.keystone.coinlib.coins.polkadot.pallets.multisig.AsMulti;
import com.keystone.coinlib.coins.polkadot.pallets.multisig.AsMultiThreshold1;
import com.keystone.coinlib.coins.polkadot.pallets.multisig.CancelAsMulti;
import com.keystone.coinlib.coins.polkadot.pallets.proxy.AddProxy;
import com.keystone.coinlib.coins.polkadot.pallets.session.SetKeys;
import com.keystone.coinlib.coins.polkadot.pallets.staking.Bond;
import com.keystone.coinlib.coins.polkadot.pallets.staking.BondExtra;
import com.keystone.coinlib.coins.polkadot.pallets.staking.CancelDeferredSlash;
import com.keystone.coinlib.coins.polkadot.pallets.staking.Chill;
import com.keystone.coinlib.coins.polkadot.pallets.staking.ForceNewEra;
import com.keystone.coinlib.coins.polkadot.pallets.staking.ForceNewEraAlways;
import com.keystone.coinlib.coins.polkadot.pallets.staking.ForceNoEras;
import com.keystone.coinlib.coins.polkadot.pallets.staking.ForceUnstake;
import com.keystone.coinlib.coins.polkadot.pallets.staking.IncreaseValidatorCount;
import com.keystone.coinlib.coins.polkadot.pallets.staking.Nominate;
import com.keystone.coinlib.coins.polkadot.pallets.staking.PayoutStakers;
import com.keystone.coinlib.coins.polkadot.pallets.staking.ReapStash;
import com.keystone.coinlib.coins.polkadot.pallets.staking.Rebond;
import com.keystone.coinlib.coins.polkadot.pallets.staking.ScaleValidatorCount;
import com.keystone.coinlib.coins.polkadot.pallets.staking.SetController;
import com.keystone.coinlib.coins.polkadot.pallets.staking.SetHistoryDepth;
import com.keystone.coinlib.coins.polkadot.pallets.staking.SetInvulnerables;
import com.keystone.coinlib.coins.polkadot.pallets.staking.SetPayee;
import com.keystone.coinlib.coins.polkadot.pallets.staking.SetValidatorCount;
import com.keystone.coinlib.coins.polkadot.pallets.staking.Unbond;
import com.keystone.coinlib.coins.polkadot.pallets.staking.Validate;
import com.keystone.coinlib.coins.polkadot.pallets.staking.WithdrawUnbonded;
import com.keystone.coinlib.coins.polkadot.pallets.treasury.ProposeBounty;
import com.keystone.coinlib.coins.polkadot.pallets.treasury.ProposeSpend;
import com.keystone.coinlib.coins.polkadot.pallets.treasury.ReportAwesome;
import com.keystone.coinlib.coins.polkadot.pallets.utility.Batch;
import com.keystone.coinlib.coins.polkadot.pallets.utility.BatchAll;
import com.keystone.coinlib.exception.InvalidTransactionException;
import com.keystone.coinlib.interfaces.Coin;
import com.keystone.coinlib.utils.B58;
import com.keystone.coinlib.utils.Coins;

import org.bouncycastle.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Dot extends AbsCoin {
    public static final Map<Integer, Pallet<? extends Parameter>> pallets = new HashMap<>();
    private static void registerStaking() {
        pallets.put(0x0700, new Bond(Network.POLKADOT, 0x0700));
        pallets.put(0x0701, new BondExtra(Network.POLKADOT, 0x0701));
        pallets.put(0x0702, new Unbond(Network.POLKADOT, 0x0702));
        pallets.put(0x0703, new WithdrawUnbonded(Network.POLKADOT, 0x0703));
        pallets.put(0x0704, new Validate(Network.POLKADOT, 0x0704));
        pallets.put(0x0705, new Nominate(Network.POLKADOT, 0x0705));
        pallets.put(0x0706, new Chill(Network.POLKADOT, 0x0706));
        pallets.put(0x0707, new SetPayee(Network.POLKADOT, 0x0707));
        pallets.put(0x0708, new SetController(Network.POLKADOT, 0x0708));
        pallets.put(0x0709, new SetValidatorCount(Network.POLKADOT, 0x0709));
        pallets.put(0x070a, new IncreaseValidatorCount(Network.POLKADOT, 0x070a));
        pallets.put(0x070b, new ScaleValidatorCount(Network.POLKADOT, 0x070b));
        pallets.put(0x070c, new ForceNoEras(Network.POLKADOT, 0x070c));
        pallets.put(0x070d, new ForceNewEra(Network.POLKADOT, 0x070d));
        pallets.put(0x070e, new SetInvulnerables(Network.POLKADOT, 0x070e));
        pallets.put(0x070f, new ForceUnstake(Network.POLKADOT, 0x070f));
        pallets.put(0x0710, new ForceNewEraAlways(Network.POLKADOT, 0x0710));
        pallets.put(0x0711, new CancelDeferredSlash(Network.POLKADOT, 0x0711));
        pallets.put(0x0712, new PayoutStakers(Network.POLKADOT, 0x0712));
        pallets.put(0x0713, new Rebond(Network.POLKADOT, 0x0713));
        pallets.put(0x0714, new SetHistoryDepth(Network.POLKADOT, 0x0714));
        pallets.put(0x0715, new ReapStash(Network.POLKADOT, 0x0715));
    }
    private static void registerDemocracy() {
        pallets.put(0x0e00, new Propose(Network.POLKADOT, 0x0e00));
        pallets.put(0x0e01, new Second(Network.POLKADOT, 0x0e01));
        pallets.put(0x0e02, new Vote(Network.POLKADOT, 0x0e02));
        pallets.put(0x0e03, new EmergencyCancel(Network.POLKADOT, 0x0e03));
        pallets.put(0x0e04, new ExternalPropose(Network.POLKADOT, 0x0e04));
        pallets.put(0x0e05, new ExternalProposeMajority(Network.POLKADOT, 0x0e05));
        pallets.put(0x0e06, new ExternalProposeDefault(Network.POLKADOT, 0x0e06));
        pallets.put(0x0e07, new FastTrack(Network.POLKADOT, 0x0e07));
        pallets.put(0x0e08, new VetoExternal(Network.POLKADOT, 0x0e08));
        pallets.put(0x0e09, new CancelReferendum(Network.POLKADOT, 0x0e09));
        pallets.put(0x0e0a, new CancelQueued(Network.POLKADOT, 0x0e0a));
        pallets.put(0x0e0b, new Delegate(Network.POLKADOT, 0x0e0b));
        pallets.put(0x0e0c, new UnDelegate(Network.POLKADOT, 0x0e0c));
        pallets.put(0x0e0d, new ClearPublicProposals(Network.POLKADOT, 0x0e0d));
        pallets.put(0x0e0e, new NotePreimage(Network.POLKADOT, 0x0e0e));
        pallets.put(0x0e0f, new NotePreimageOperational(Network.POLKADOT, 0x0e0f));
        pallets.put(0x0e10, new NoteImminentPreimage(Network.POLKADOT, 0x0e10));
        pallets.put(0x0e11, new NoteImminentPreimageOperational(Network.POLKADOT, 0x0e11));
        pallets.put(0x0e12, new ReapPreimage(Network.POLKADOT, 0x0e12));
        pallets.put(0x0e13, new Unlock(Network.POLKADOT, 0x0e13));
        pallets.put(0x0e14, new RemoveVote(Network.POLKADOT, 0x0e14));
        pallets.put(0x0e15, new RemoveOtherVote(Network.POLKADOT, 0x0e15));
        pallets.put(0x0e16, new EnactProposal(Network.POLKADOT, 0x0e16));
        pallets.put(0x0e17, new Blacklist(Network.POLKADOT, 0x0e17));
        pallets.put(0x0e18, new CancelProposal(Network.POLKADOT, 0x0e18));
    }
    private static void registerElectionsPhragmen() {
        pallets.put(0x1100,
                new com.keystone.coinlib.coins.polkadot.pallets.elections_phragmen.Vote(Network.POLKADOT, 0x1100));
        pallets.put(0x1101, new RemoveVoter(Network.POLKADOT, 0x1101));
        pallets.put(0x1102, new SubmitCandidacy(Network.POLKADOT, 0x1102));
        pallets.put(0x1103, new RenounceCandidacy(Network.POLKADOT, 0x1103));
        pallets.put(0x1104, new RemoveMember(Network.POLKADOT, 0x1104));
    }
    static {
        pallets.put(0x0500, new Transfer(Network.POLKADOT, 0x0500));
        pallets.put(0x0503, new TransferKeepAlive(Network.POLKADOT, 0x0503));
        pallets.put(0x0900, new SetKeys(Network.POLKADOT, 0x0900));

        registerStaking();
        registerDemocracy();
        registerElectionsPhragmen();


        pallets.put(0x1c01, new SetIdentity(Network.POLKADOT, 0x1c01));

        pallets.put(0x1d01, new AddProxy(Network.POLKADOT, 0x1d01));

        pallets.put(0x1a00, new Batch(Network.POLKADOT, 0x1a00));
        pallets.put(0x1a02, new BatchAll(Network.POLKADOT, 0x1a02));
        pallets.put(0x1300, new ProposeSpend(Network.POLKADOT, 0x1300));
        pallets.put(0x1303, new ReportAwesome(Network.POLKADOT, 0x1303));
        pallets.put(0x1308,new ProposeBounty(Network.POLKADOT, 0x1308));

        registerMultisig();
    }

    private static void registerMultisig() {
        pallets.put(0x1e00, new AsMultiThreshold1(Network.POLKADOT, 0x1e00));
        pallets.put(0x1e01, new AsMulti(Network.POLKADOT, 0x1e01));
        pallets.put(0x1e02, new ApproveAsMulti(Network.POLKADOT, 0x1e02));
        pallets.put(0x1e03, new CancelAsMulti(Network.POLKADOT, 0x1e03));
    }

    public Dot(Coin impl) {
        super(impl);
    }

    @Override
    public String coinCode() {
        return Coins.DOT.coinCode();
    }

    public static class Tx extends AbsTx {

        public Tx(JSONObject object, String coinCode) throws JSONException, InvalidTransactionException {
            super(object, coinCode);
        }

        @Override
        protected void parseMetaData() throws JSONException {
            to = metaData.getString("dest");
            amount = metaData.getLong("value") / Math.pow(10, decimal);
            fee = metaData.optLong("tip",0) / Math.pow(10, decimal);

            if (!metaData.has("nonce")) {
                metaData.put("nonce",0);
            }
            if (!metaData.has("implVersion")) {
                metaData.put("implVersion",0);
            }
            if (!metaData.has("authoringVersion")) {
                metaData.put("authoringVersion",0);
            }
            metaData.put("eraPeriod",4096);
        }

        @Override
        protected void checkHdPath() throws InvalidTransactionException {
            Coins.Coin coin = Coins.SUPPORTED_COINS.stream()
                    .filter(c->c.coinCode().equals(coinCode))
                    .findFirst().orElse(null);

             if(coin == null || !hdPath.equals(coin.getAccounts()[0])) {
                 throw new InvalidTransactionException(String.format("invalid hdPath \"%s\" for %s", hdPath, coinCode));
             }
        }
    }

    public static class Deriver extends AbsDeriver {
        protected byte prefix = 0;
        @Override
        public String derive(String xPubKey, int changeIndex, int addrIndex) {
            byte[] bytes = new B58().decode(xPubKey);
            byte[] pubKey = Arrays.copyOfRange(bytes,bytes.length - 4 - 32,bytes.length - 4);
            return AddressCodec.encodeAddress(pubKey, prefix);
        }

        @Override
        public String derive(String xPubKey) {
            byte[] bytes = new B58().decode(xPubKey);
            byte[] pubKey = Arrays.copyOfRange(bytes,bytes.length - 4 - 32,bytes.length - 4);
            return AddressCodec.encodeAddress(pubKey, prefix);
        }

        @Override
        public String derive(String xPubKey, int index) {
            throw new RuntimeException("not implemented");
        }
    }
}
