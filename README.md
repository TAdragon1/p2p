Each of the config files is specific to a peer. On the peer servers themselves the number suffix is removed.
    For example config_peer10.txt is config_peer.txt on eecslab-10.case.edu

To run: java p2p

4 Commands available:
    Connect
        Connects to neighbors in config file.
    Get filename.filetype
        Queries peers for the file (must include extension). If file is found on another peer, the file is
        downloaded directly from that peer and stored in the obtained directory.
    Leave
        Closes neighbor connections but does not quit.
    Exit
        Closes all sockets (neighbors and hosts) and exits. 

I do not block my peers from receiving duplicate responses, but I DO block them from sending/forwarding duplicate responses.