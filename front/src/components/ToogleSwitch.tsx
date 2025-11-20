import styled from "styled-components";
import theme from "../styles/theme";

interface ToggleSwitchProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
}

export default function ToggleSwitch({ checked, onChange }: ToggleSwitchProps) {
  return (
    <SwitchWrapper onClick={() => onChange(!checked)}>
      <SwitchTrack checked={checked}>
        <SwitchThumb checked={checked} />
      </SwitchTrack>
    </SwitchWrapper>
  );
}

const SwitchWrapper = styled.div`
  cursor: pointer;
  display: inline-flex;
  align-items: center;
`;

const SwitchTrack = styled.div<{ checked: boolean }>`
  width: 44px;
  height: 24px;
  background-color: ${({ checked }) =>
    checked ? theme.colors.primary : theme.colors.grayscale.g400};
  border-radius: 24px;
  padding: 3px;
  transition: background-color 0.2s ease;
`;

const SwitchThumb = styled.div<{ checked: boolean }>`
  width: 18px;
  height: 18px;
  background-color: white;
  border-radius: 50%;
  transition: transform 0.2s ease;
  transform: ${({ checked }) =>
    checked ? "translateX(20px)" : "translateX(0px)"};
`;
