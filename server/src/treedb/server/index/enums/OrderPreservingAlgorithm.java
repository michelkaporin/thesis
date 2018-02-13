package treedb.server.index.enums;

import com.google.gson.annotations.SerializedName;

public enum OrderPreservingAlgorithm {
    @SerializedName(value = "ore", alternate = {"ORE"})
    ORE, 
    @SerializedName(value = "ope", alternate = {"OPE"})
    OPE;
}