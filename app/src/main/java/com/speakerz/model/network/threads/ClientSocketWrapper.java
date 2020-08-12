package com.speakerz.model.network.threads;

import com.speakerz.model.network.threads.audio.ClientAudioMultiCastReceiverSocketThread;

public class ClientSocketWrapper {
   public ClientControllerSocketThread controllerSocket;
   public ClientAudioMultiCastReceiverSocketThread audioSocket;
}
