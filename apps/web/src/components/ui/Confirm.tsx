import * as Dialog from "@radix-ui/react-dialog";
export function Confirm({
  open,
  onClose,
  onConfirm,
  title,
  description,
  pending = false,
}: {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  description: string;
  pending?: boolean;
}) {
  return (
    <Dialog.Root open={open} onOpenChange={(value) => !value && onClose()}>
      <Dialog.Portal>
        <Dialog.Overlay className="overlay" />
        <Dialog.Content className="dialog">
          <Dialog.Title>{title}</Dialog.Title>
          <Dialog.Description>{description}</Dialog.Description>
          <div className="actions">
            <Dialog.Close className="button secondary">Cancelar</Dialog.Close>
            <button
              className="button danger"
              disabled={pending}
              onClick={onConfirm}
            >
              {pending ? "Excluindo…" : "Confirmar exclusão"}
            </button>
          </div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
