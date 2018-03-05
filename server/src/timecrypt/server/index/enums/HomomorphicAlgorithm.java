package timecrypt.server.index.enums;

import com.google.gson.annotations.SerializedName;

public enum HomomorphicAlgorithm {
    @SerializedName(value = "paillier", alternate = {"PAILLIER"})
    PAILLIER, 
    @SerializedName(value = "ecelgamal", alternate = {"ECELGAMAL"})
    ECELGAMAL;
}