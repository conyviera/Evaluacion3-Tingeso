import * as React from 'react';
import PropTypes from 'prop-types';
import { Box } from '@mui/material';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import MuiModal from '@mui/material/Modal';
import IconButton from '@mui/material/IconButton';
import CloseIcon from '@mui/icons-material/Close';

const style = {
  position: 'absolute',
  top: '50%',
  left: '50%',
  transform: 'translate(-50%, -50%)',
  width: 500,
  bgcolor: "#EAEAD1",
  boxShadow: 20,
  p: 2,
  borderRadius: 10,
};

function Modal({ button, children }) {
  const [open, setOpen] = React.useState(false);
  const handleOpen = () => setOpen(true);
  const handleClose = () => setOpen(false);

  return (
    <div>
      {React.cloneElement(button, {
        onClick: handleOpen,
      })}
      <MuiModal
        open={open}
        onClose={handleClose}
      >
        <Box sx={style}>
          <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
            <IconButton
              aria-label="close"
              onClick={handleClose}
              sx={{
                color: (theme) => theme.palette.grey[500],
              }}
            >
              <CloseIcon />
            </IconButton>
          </Box>
          <Box sx={{ maxHeight: '100%', overflowY: 'auto' }}>
            {children}
          </Box>
        </Box>
      </MuiModal>
    </div>
  );
}

Modal.propTypes = {
  button: PropTypes.node.isRequired,
  children: PropTypes.node,
};

export default Modal;