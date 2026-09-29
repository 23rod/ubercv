package modelo;

public class Usuario {
    private String correo;
    private String hashClave;
    private byte[] sal;
    private String cedula;
    private Rol rol;

    public Usuario(String correo, String hashClave, byte[] sal, String cedula, Rol rol) {
        this.correo = correo;
        this.hashClave = hashClave;
        this.sal = sal;
        this.cedula = cedula;
        this.rol = rol;
    }

    public String getCorreo() { return correo; }
    public String getHashClave() { return hashClave; }
    public byte[] getSal() { return sal; }
    public String getCedula() { return cedula; }
    public Rol getRol() { return rol; }
}