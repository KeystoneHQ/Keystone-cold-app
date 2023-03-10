package com.keystone.cold.remove_wallet_mode.viewmodel.sync_viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.keystone.coinlib.accounts.ExtendedPublicKey;
import com.keystone.coinlib.utils.Coins;
import com.keystone.cold.AppExecutors;
import com.keystone.cold.DataRepository;
import com.keystone.cold.MainApplication;
import com.keystone.cold.callables.GetExtendedPublicKeyCallable;
import com.keystone.cold.db.entity.AddressEntity;
import com.sparrowwallet.hummingbird.UR;
import com.sparrowwallet.hummingbird.registry.RegistryType;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborEncoder;
import co.nstant.in.cbor.CborException;

public class XRPToolkitViewModel extends AndroidViewModel {

    protected List<Long> addressIds;
    private DataRepository mRepository;

    public void setAddressIds(List<Long> addressIds) {
        this.addressIds = addressIds;
    }


    public XRPToolkitViewModel(@NonNull Application application) {
        super(application);
        mRepository = MainApplication.getApplication().getRepository();
    }

    public MutableLiveData<UR> generateSyncUR() {
        MutableLiveData<UR> ur = new MutableLiveData<>();
        AppExecutors.getInstance().diskIO().execute(() -> {
            AddressEntity addressEntity;
            if (addressIds == null) {
                addressEntity = mRepository.loadAddressSync(Coins.XRP.coinId()).get(0);
            } else {
                addressEntity = mRepository.loadAddressById(addressIds.get(0));
            }
            String xpub = new GetExtendedPublicKeyCallable(addressEntity.getPath()).call();
            String pubkey = Hex.toHexString(new ExtendedPublicKey(xpub).getKey());
            JSONObject object = new JSONObject();
            try {
                object.put("address", addressEntity.getAddressString())
                        .put("pubkey", pubkey);
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    (new CborEncoder(baos)).encode((new CborBuilder()).add(object.toString().getBytes(StandardCharsets.UTF_8)).build());
                    byte[] cbor = baos.toByteArray();
                    ur.postValue(new UR(RegistryType.BYTES, cbor));
                } catch (CborException | UR.InvalidTypeException var4) {
                    var4.printStackTrace();
                    ur.postValue(null);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ur.postValue(null);
            }
        });
        return ur;
    }
}
