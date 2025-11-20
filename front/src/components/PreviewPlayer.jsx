import { useEffect, useState } from "react";

export default function PreviewPlayer({ previewUrl }) {
  return (
    <audio controls src={previewUrl} autoPlay />
  );
}
