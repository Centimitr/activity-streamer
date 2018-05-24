public interface IRecoverable {
    String snapshot();

    void recover(String snapshot);
}
