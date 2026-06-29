package org.esupportail.activbo.domain.beans.channels;

import java.io.Serial;

public class ChannelException extends Exception {
    /**
     * The id for serialization.
     */
    @Serial private static final long serialVersionUID = 8197090501242229324L;

    public ChannelException(final String message) {
        super(message);
    }

}
