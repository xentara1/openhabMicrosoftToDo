package org.openhab.binding.microsofttodo.internal.microsoft;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.graph.models.extensions.DirectoryObject;
import com.microsoft.graph.serializer.IJsonBackedObject;

public class Task extends DirectoryObject implements IJsonBackedObject {

    @SerializedName("subject")
    @Expose
    String subject;
    @SerializedName("owner")
    @Expose
    String owner;
    @SerializedName("status")
    @Expose
    String status;
}
